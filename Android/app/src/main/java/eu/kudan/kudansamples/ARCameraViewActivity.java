package eu.kudan.kudansamples;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jme3.math.Vector3f;

import eu.kudan.kudan.ARActivity;
import eu.kudan.kudan.ARArbiTrack;
import eu.kudan.kudan.ARGyroPlaceManager;
import eu.kudan.kudan.ARImageNode;
import eu.kudan.kudan.ARLightMaterial;
import eu.kudan.kudan.ARMeshNode;
import eu.kudan.kudan.ARModelImporter;
import eu.kudan.kudan.ARModelNode;
import eu.kudan.kudan.ARTexture2D;

public class ARCameraViewActivity extends ARActivity {

    private  ARModelNode modelNode;
    private ARBITRACK_STATE arbitrack_state;


    //Tracking enum
    enum ARBITRACK_STATE {
        ARBI_PLACEMENT,
        ARBI_TRACKING
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcamera_view);
        arbitrack_state  = ARBITRACK_STATE.ARBI_PLACEMENT;
    }

    public void setup() {
        addModelNode();
        setupArbiTrack();

    }


    private  void addModelNode() {

        // Import model
        ARModelImporter modelImporter = new ARModelImporter();
        modelImporter.loadFromAsset("arrow4.jet");
//        modelNode = (ARModelNode) modelImporter.getNode();
        modelNode = (ARModelNode) modelImporter.getNode();
        modelNode.rotateByDegrees(90,1,0,0);
        modelNode.rotateByDegrees(270,0,0,1);

//         Load model texture
        ARTexture2D texture2D = new ARTexture2D();
        texture2D.loadFromAsset("bigBenTexture.png");

        // Apply model texture to model texture material
        ARLightMaterial material = new ARLightMaterial();
        material.setTexture(texture2D);
        material.setAmbient(0.8f, 0.8f, 0.8f);

//         Apply texture material to models mesh nodes
        for (ARMeshNode meshNode : modelImporter.getMeshNodes()) {
            meshNode.setMaterial(material);
        }

        modelNode.scaleByUniform(500f);
        modelNode.scaleBy(1.5f,1,1);

    }

    //Sets up arbi track
    public void setupArbiTrack() {


        // Initialise gyro placement. Gyro placement positions content on a virtual floor plane where the device is aiming.
        ARGyroPlaceManager gyroPlaceManager = ARGyroPlaceManager.getInstance();
        Vector3f floor = gyroPlaceManager.getWorld().getPosition();
        Vector3f floorOffset = new Vector3f(0,gyroPlaceManager.getFloorDepth(),0);
        floor = floor.add(floorOffset);
        gyroPlaceManager.initialise();

        // Create an image node to be used as a target node
        ARImageNode targetImageNode = new ARImageNode("target.png");

        Log.i("tagger",floor.toString());
        Log.i("tagger",gyroPlaceManager.getFloorDepth()+"");

        // Scale and rotate the image to the correct transformation.
        targetImageNode.scaleByUniform(0.3f);
        targetImageNode.rotateByDegrees(90, 1, 0, 0);
        targetImageNode.setPosition(floor);

        // Add target node to gyro place manager
        gyroPlaceManager.getWorld().addChild(targetImageNode);

        // Initialise the arbiTracker
        ARArbiTrack arbiTrack = ARArbiTrack.getInstance();
        arbiTrack.initialise();

        // Set the arbiTracker target node to the node moved by the user.
        arbiTrack.setTargetNode(targetImageNode);

        // Add model node to world
        modelNode.setPosition(new Vector3f(0,gyroPlaceManager.getFloorDepth()-500,0));
        arbiTrack.getWorld().addChild(modelNode);
    }

    public void lockPosition(View view) {

        Button b = (Button)findViewById(R.id.lockButton);
        ARArbiTrack arbiTrack = ARArbiTrack.getInstance();
        // If in placement mode start arbi track, hide target node and alter label
        if(arbitrack_state.equals(ARBITRACK_STATE.ARBI_PLACEMENT)) {

            //Start Arbi Track
            arbiTrack.start();

            //Hide target node
            arbiTrack.getTargetNode().setVisible(false);

            //Change enum and label to reflect Arbi Track state
            arbitrack_state = ARBITRACK_STATE.ARBI_TRACKING;
            b.setText("Stop Tracking");


        }

        // If tracking stop tracking, show target node and alter label
        else {

            // Stop Arbi Track
            arbiTrack.stop();

            // Display target node
            arbiTrack.getTargetNode().setVisible(true);

            //Change enum and label to reflect Arbi Track state
            arbitrack_state = ARBITRACK_STATE.ARBI_PLACEMENT;
            b.setText("Start Tracking");

        }

    }
}
