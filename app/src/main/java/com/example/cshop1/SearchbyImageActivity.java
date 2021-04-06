package com.example.cshop1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;



public class SearchbyImageActivity extends AppCompatActivity {

    String lid;
    Button button;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchby_image);

        OpenCVLoader.initDebug();
        button = findViewById(R.id.button);
        imageView = findViewById(R.id.image_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Search By Image");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);



        Intent intent = getIntent();
        lid = intent.getStringExtra("uid");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hello();
            }
        });

    }

    private void hello(){
        Mat emptyMat = new Mat(500,500, CvType.CV_8UC3);
        Imgproc.circle(emptyMat,new Point(250,250),25,new Scalar(255,255,255));
        Bitmap bitmap = Bitmap.createBitmap(500,500, Bitmap.Config.RGB_565);
        Utils.matToBitmap(emptyMat,bitmap);
        imageView.setImageBitmap(bitmap);
    }

    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}