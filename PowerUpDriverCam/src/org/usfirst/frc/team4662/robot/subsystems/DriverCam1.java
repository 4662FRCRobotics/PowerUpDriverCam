package org.usfirst.frc.team4662.robot.subsystems;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 * DiverCam
 * 
 * This class provides a thread sending one of two selectable USB cameras to the dashboard.
 * 
 * No arguments
 * 
 * toggleCamera() public method to toggles cameras.
 */
public class DriverCam1 extends Subsystem {
	
	Thread visionThread;
	private boolean m_isCameraA;
	
	public DriverCam1() {
		
		m_isCameraA = false; 
		
		visionThread = new Thread(() -> {

			UsbCamera CamA = new UsbCamera ( "USB Camera 0", 0);
			
			CamA.setResolution(320, 240);
			CamA.setFPS(10);
			
			//CvSinks capture Mats from the camera.
			CvSink cvSinkCamA = CameraServer.getInstance().getVideo(CamA);
			//CvSourc sends images back to the Dashboard
			CvSource outputStream = CameraServer.getInstance().putVideo("DriveCam", 320, 240);
		
			//Mats are very memory expensive. Reuse this Mat.
			Mat mat = new Mat();
			cvSinkCamA.setEnabled(true);
			
			//Cannot be "true" bc program will not exist. 
			//Lets robot stop thread when restarting robot code or deploying
			while (!Thread.interrupted()) {
				
				if(cvSinkCamA.grabFrame(mat) == 0) {
					// Send the output the error
					outputStream.notifyError(cvSinkCamA.getError());
					continue;
				}							
				outputStream.putFrame(mat);
			}
		});
		visionThread.setDaemon(true);
		visionThread.start();
	}

    public void initDefaultCommand() {
        // No default
    }
    
    public void toggleCamera() {
    	m_isCameraA = !m_isCameraA;
    }
}

