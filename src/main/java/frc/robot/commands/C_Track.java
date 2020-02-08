/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import org.frcteam2910.common.math.Vector2;
import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.drivers.Vision;
import frc.robot.subsystems.SS_Drivebase;



public class C_Track extends CommandBase {

  private Vision vision;

  private static final double p = 1000;

  private DoubleSupplier forward;
  private DoubleSupplier strafe;

  private SS_Drivebase drivebase;


  public C_Track(Vision vision, SS_Drivebase drivebase, DoubleSupplier forward, DoubleSupplier strafe) {

    
    this.forward = forward;
    this.strafe = strafe;
    this.drivebase = drivebase;
    this.vision = vision;
    
    addRequirements(drivebase);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {

      SmartDashboard.putNumber("vision X with P",vision.getXOffset()/p);
      if(vision.getValidTarget()){
        drivebase.drive(new Vector2(forward.getAsDouble(), strafe.getAsDouble()), vision.getXOffset()/p, false);
      }

    
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }

}
