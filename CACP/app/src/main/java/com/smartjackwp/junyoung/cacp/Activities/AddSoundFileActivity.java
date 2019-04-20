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

    final String[] formats = {".mp3", ".wav"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sound_file);

        cacp = ChineseAccentComparison.getInstance(this);

        loadFileButton = findViewById(R.id.loadFileButton);
        addFileButton = findViewById(R.id.addFileButton);
        fileNameTextView = findViewById(R.id.fileNameTextView);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        File mPath = Environment.getExternalStorageDirectory();
        fileDialog = new FileDialog(AddSoundFileActivity.this, mPath, formats);
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                if(checkFormat(file.toString()))
                {
                    fileNameTextView.setText(file.toString());
                    filePath = file.toString();
                }
                else
                    Toast.makeText(AddSoundFileActivity.this, "유효한 파일이 아닙니다.", Toast.LENGTH_LONG).show();
            }
        });

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
                    if(title.length() <= 0)
                        title = new File(filePath).getName();

                    int id = cacp.saveContents(new AccentContents(filePath, title, description));

                    if(id > -1)
                        returnResult(id);
                    else
                        Toast.makeText(AddSoundFileActivity.this, "음성 파일 추가를 실패하였습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //컨텐츠 등록 폼의 유효성 검사
    private boolean checkValidation()
    {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        if(filePath == null || filePath.length() <= 0)
        {
            Toast.makeText(this, "파일을 선택하세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        /*
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
        */

        return true;
    }

    //선택 오디오 파일 포멧 검사
    private boolean checkFormat(String fileName)
    {
        for(String format: formats)
            if (fileName.endsWith(format))
                return true;
        return false;
    }

    private void returnResult(int id)
    {
        Intent intent = new Intent();
        intent.putExtra(AccentContents._ID, id);

        setResult(RESULT_OK, intent);
        finish();
    }
}
