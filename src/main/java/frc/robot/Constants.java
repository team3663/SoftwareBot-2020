/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

public class Constants {
    public static final class DriveConstants {
        public static final int SHORT_SOLENOID_FORWARD = 0;
        public static final int SHORT_SOLENOID_REVERSE = 1;

        public static final int LONG_SOLENOID_FORWARD = 2;
        public static final int LONG_SOLENOID_REVERSE = 3;
        
        public static final int powerCellPickUpMotor = 10;
    }

    public static final class OIConstants {
        public static final int kDriverControllerPort = 0;
        
        public static final int L_STICK_X_AXIS = 0;
        public static final int L_STICK_Y_AXIS = 1;
        public static final int L_TRIGGER_AXIS = 2;
        public static final int R_TRIGGER_AXIS = 3;
        public static final int R_STICK_X_AXIS = 4;
        public static final int R_STICK_Y_AXIS = 5;

        public static final int BUTTON_A = 1;
        public static final int BUTTON_B = 2;
        public static final int BUTTON_X = 3;
        public static final int BUTTON_Y = 4;
        public static final int L_BUMPER = 5;
        public static final int R_BUMPER = 6;
        public static final int BUTTON_BACK = 7;
        public static final int BUTTON_START = 8;
        public static final int L_JOYCLICK = 9;
        public static final int R_JOYCLICK = 10;
    }
}
