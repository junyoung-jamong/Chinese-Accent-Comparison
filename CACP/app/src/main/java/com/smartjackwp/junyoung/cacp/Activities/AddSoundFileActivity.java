package com.smartjackwp.junyoung.cacp.Activities;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.smartjackwp.junyoung.cacp.ChineseAccentComparison;
import com.smartjackwp.junyoung.cacp.CustomizedUI.FileDialog;
import com.smartjackwp.junyoung.cacp.Entity.AccentContents;
import com.smartjackwp.junyoung.cacp.R;

import java.io.File;

public class AddSoundFileActivity extends AppCompatActivity {
    Button loadFileButton;
    Button addFileButton;
    TextView fileNameTextView;
    EditText titleEditText;
    EditText descriptionEditText;

    FileDialog fileDialog;

    String filePath;

    ChineseAccentComparison cacp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sound_file);

        cacp = ChineseAccentComparison.getInstance();

        loadFileButton = findViewById(R.id.loadFileButton);
        addFileButton = findViewById(R.id.addFileButton);
        fileNameTextView = findViewById(R.id.fileNameTextView);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        File mPath = new File(Environment.getExternalStorageDirectory() + "//");
        fileDialog = new FileDialog(AddSoundFileActivity.this, mPath);
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                fileNameTextView.setText(file.toString());
                filePath = file.toString();
            }
        });

        //fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
        //  public void directorySelected(File directory) {
        //      Log.d(getClass().getName(), "selected dir " + directory.toString());
        //  }
        //});
        //fileDialog.setSelectDirectoryOption(false);

        //파일 버튼(파일 찾기) 클릭 시
        loadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileDialog.showDialog();
            }
        });

        //등록하기 버튼 클릭 시
        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkValidation())
                {
                    String title = titleEditText.getText().toString();
                    String description = descriptionEditText.getText().toString();
                    //cacp.saveSoundFile(filePath, title, description);
                    returnResult(filePath, title, description);
                }
            }
        });
    }

    private boolean checkValidation()
    {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        if(filePath == null || filePath.length() <= 0)
        {
            Toast.makeText(this, "파일을 선택하세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(title.length() <= 0)
        {
            Toast.makeText(this, "제목을 입력하세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(description.length() <= 0)
        {
            Toast.makeText(this, "설명을 입력하세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void returnResult(String filePath, String title, String description)
    {
        Intent intent = new Intent();
        intent.putExtra(AccentContents.FILE_PATH, filePath);
        intent.putExtra(AccentContents.TITLE, title);
        intent.putExtra(AccentContents.DESCRIPTION, description);

        setResult(RESULT_OK, intent);
        finish();
    }
}
