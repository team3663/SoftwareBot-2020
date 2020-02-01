/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import org.frcteam2910.common.math.Rotation2;
import org.frcteam2910.common.robot.input.Axis;
import org.frcteam2910.common.robot.input.Controller;
import org.frcteam2910.common.robot.input.XboxController;

import edu.wpi.first.wpilibj2.command.button.Button;
import edu.wpi.first.wpilibj2.command.InstantCommand;

/**
 * Add your docs here.
 */
public class OI {
    private XboxController controller = new XboxController(RobotMap.DRIVE_CONTROLLER_ID);

    public OI() {
        controller.getRightXAxis().setScale(0.25);
        controller.getRightXAxis().setInverted(true);
    }

    public void bindButtons() {
        getResetGyroButton().whenPressed(new InstantCommand(() ->
                Robot.getDrivebase().resetGyroAngle(Rotation2.ZERO)
        ));
    }

    public Axis getDriveForwardAxis() {
        return controller.getLeftYAxis();
    }

    public Axis getDriveStrafeAxis() {
        return controller.getLeftXAxis();
    }

    public Axis getDriveRotationAxis() {
        return controller.getRightXAxis();
    }

    public Button getResetGyroButton() {
        return controller.getBackButton();
    }
    public XboxController getController() {
        return controller;
    }
}