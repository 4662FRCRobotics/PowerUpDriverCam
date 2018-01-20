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
public class DriverCam extends Subsystem {
	
	Thread visionThread;
	private boolean m_isCameraA;
	
	public DriverCam() {
		
		m_isCameraA = false; 
		
		visionThread = new Thread(() -> {

			UsbCamera CamA = new UsbCamera ( "USB Camera 0", 0);
			UsbCamera CamB = new UsbCamera ( "USB Camera 1", 1);
			
			CamA.setResolution(640, 360);
			CamA.setFPS(15);
			CamB.setResolution(640, 360);
			CamB.setFPS(15);
			
			//CvSinks capture Mats from the camera.
			CvSink cvSinkCamA = CameraServer.getInstance().getVideo(CamA);
			CvSink cvSinkCamB = CameraServer.getInstance().getVideo(CamB);
			//CvSourc sends images back to the Dashboard
			CvSource outputStream = CameraServer.getInstance().putVideo("DriveCam", 640, 360);
		
			//Mats are very memory expensive. Reuse this Mat.
			Mat mat = new Mat();
			
			//Cannot be "true" bc program will not exist. 
			//Lets robot stop thread when restarting robot code or deploying
			while (!Thread.interrupted()) {
				if ( m_isCameraA ) {
					cvSinkCamB.setEnabled(false);
					cvSinkCamA.setEnabled(true);
					if(cvSinkCamA.grabFrame(mat) == 0) {
						// Send the output the error
						outputStream.notifyError(cvSinkCamA.getError());
						continue;
					}
				} else {
					cvSinkCamA.setEnabled(false);
					cvSinkCamB.setEnabled(true);
					if(cvSinkCamB.grabFrame(mat) == 0) {
						// Send the output the error
						outputStream.notifyError(cvSinkCamB.getError());
						continue;
					} else {
						Core.transpose(mat, mat);
						Core.flip(mat, mat, 0);
						Imgproc.rectangle(mat, new Point(60, 20), new Point(160, 40),
								new Scalar(255, 255, 255), 5);
					}
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

