/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.CANEncoder;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;


public class SS_Shooter extends SubsystemBase {

  //Known RPMs for different distances. Column one: feet, Column two: RPM. 0 feet is parked directly infront of the power port
  private final int[][] KNOWN_RPM = new int[][] {
    {0, 0},
    {10, 3100},
    {15, 3200},
    {20, 3500},
    {25, 4000},
    {30, 4500},
    {35, 5500}
  };
  private final int DISTANCE_COLUMN = 0; //column index for distance values
  private final int RPM_COLUMN = 1; //column index for RPM values
  private final int HOOD_FAR_DISTANCE = 10; //The distance at which the hood switches to the far angle

  //the correction multiplier in the code that is fixed (the other one, correctionMultiplier, can be changed during a match)
  private final double WHEEL_GEAR_RATIO_MULTIPLIER = 1;
  

  //Wheel PID constants (These values are tuned correctly for the software robot)
  private final double KP = 0.0005;
  private final double KI = 0.000001;
  private final double KD = 0;

  private final double CONFIDENCE_THRESHOLD = 97; //the threshold or the percent wanted to shoot at
  private final double CONFIDENCE_TIME = 1; //time we want to be in the confidence band before shooting

  private CANSparkMax wheel;
  private CANEncoder encoder;
  private CANPIDController PID;
  private DoubleSolenoid hood;

  private Timer confidenceTimer;

  private int targetRPM = 0;

  private boolean wheelSpinning = false;
  private double correctionMultiplier = 1;

  public SS_Shooter() {
    wheel = new CANSparkMax(Constants.FLY_WEEL_MOTOR, MotorType.kBrushless);
    wheel.setInverted(true);
    encoder = wheel.getEncoder();
    encoder.setVelocityConversionFactor(WHEEL_GEAR_RATIO_MULTIPLIER);
    PID = wheel.getPIDController();
    PID.setOutputRange(0, 1);

    //set Wheel PID constants
    PID.setP(KP);
    PID.setI(KI);
    PID.setD(KD);
    //hoodAngle = new DoubleSolenoid(1, 2); //TODO

    confidenceTimer = new Timer();
    confidenceTimer.start();
  }

  @Override
  public void periodic() {
    //continually update the targetRPM
    if(wheelSpinning) {
      setRPM(targetRPM);
    } else {
      setRPM(0);
    }

    //push telemetry to the smart dashboard
    SmartDashboard.putNumber("target RPM", targetRPM);
    SmartDashboard.putNumber("Current shooter RPM", getCurrentRPM());
    SmartDashboard.putNumber("shooting confidence", getShotConfidence());
    SmartDashboard.putBoolean("wheel spinning", wheelSpinning);
  }

  /**
   * start spinning the flywheel
   */
  public void startSpinning() {
    wheelSpinning = true;
  }

  /**
   * stop spinning the flywheel
   */
  public void stopSpinning() {
    wheelSpinning = false;
  }

  /**
   * Sets the distance to shoot for
   * @param targetDistance distance to shoot (in feet)
   */
  public void setTargetDistance(double targetDistance) {
    targetRPM = calculateRPM(targetDistance);
    updateHood(targetDistance);
  }

  /**
   * Sets the multiplier for correcting shooting distance
   * @param correctionMultiplier the new correction multiplier
   */
  public void setCorrectionMultiplier(double correctionMultiplier) {
    this.correctionMultiplier = correctionMultiplier;
  }

  /**
   * @return the percentage of confidence for the shot based on wheel velocity (from 0-100)
   */
  public double getShotConfidence() {
    //if the wheel is not spinning, we have no confidence in making a shot
    if(!wheelSpinning) {
      return 0;
    }

    //get percentage of current speed to target speed
    double confidence = (getCurrentRPM() / targetRPM) * 100;
    //fix percentage values over 100
    if(confidence > 100) {
      confidence = 100 - (confidence - 100);
    }

    //reset the confidence timer if we are not within the band of error percent we want
    if(confidence < CONFIDENCE_THRESHOLD) {
      confidenceTimer.reset();
    }
    //calculate the percent of the amount of time we want to be in the confidence range to how long we actually are in it
    double confidenceTimePercent = (confidenceTimer.get() / CONFIDENCE_TIME) * 100;
    //prevents from returning confidences over 100
    return Math.min(100, confidenceTimePercent);
  }

  /**
   * Returns the non-fixed correction multiplier
   * @return the correction multiplier
   */
  public double getCorrection() {
    return correctionMultiplier;
  }

  public boolean isSpinning() {
    return wheelSpinning;
  }

  /**
   * Set target RPM for the wheel
   * @param RPM target RPM for the wheel
   */
  private void setRPM(double RPM) {
    PID.setReference(RPM * correctionMultiplier, ControlType.kVelocity);
  }

  /**
   * returns the RPM of the wheel
   * @return the RPM of the wheel
   */
  private double getCurrentRPM() {
    return encoder.getVelocity();
  }

  /**
   * Convert distance to correct RPM for shooting power cells
   * @param distance distance to convert to RPMs (in feet)
   * @return RPMs converted from distance
   */
  private int calculateRPM(double distance) {
    distance = Math.round(distance); //round the distance so that 5.1 feet does not use the value for 6 feet
    int index = 0;
    for(int i = 0; i < KNOWN_RPM.length; i++) {
      double currentDistance = KNOWN_RPM[i][DISTANCE_COLUMN];
      //If the rounded distance equals a known distance, use that known distance
      if(currentDistance == distance) {
        return KNOWN_RPM[i][RPM_COLUMN];
      } else if(currentDistance > distance) {
        //if the index is at the lowest distance, return the lowest RPM
        if(i == 0) {
          return KNOWN_RPM[0][RPM_COLUMN];
        }
        index = i;
        return linearInterpolation(index, distance);
      }
    }
    return KNOWN_RPM[KNOWN_RPM.length - 1][RPM_COLUMN];
  }

  /**
   * Interpolate the RPM of that the shooter should spin based on the distance and the index of the closest greater RPM in the table
   * @param index  the index of the closest greater RPM in the table
   * @param distance the distance away from the target
   * @return the needed RPM for the distance
   */
  private int linearInterpolation(int index, double distance) {
    //find the range between the two known distances
    double distanceRange = KNOWN_RPM[index][DISTANCE_COLUMN] - KNOWN_RPM[index - 1][DISTANCE_COLUMN];
    double RPMRange = (KNOWN_RPM[index][RPM_COLUMN] - KNOWN_RPM[index - 1][RPM_COLUMN]);
    //calculate the new distance between the known distances linearly
    return (int)((distance - KNOWN_RPM[index - 1][DISTANCE_COLUMN]) / distanceRange * RPMRange) + KNOWN_RPM[index - 1][RPM_COLUMN];
  }

  private void updateHood(double targetDistance) {
    if(targetDistance >= HOOD_FAR_DISTANCE) {
      //hood.set(RobotMap.SHOOTER_FAR_ANGLE);
    } else {
      //hood.set(RobotMap.SHOOTER_NEAR_ANGLE);
    }
  }
}
