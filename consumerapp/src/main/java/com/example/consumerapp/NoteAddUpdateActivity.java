package com.example.consumerapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.example.consumerapp.entity.Note;
import com.example.consumerapp.helper.MappingHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.provider.CallLog.Calls.DATE;
import static android.provider.MediaStore.MediaColumns.TITLE;
import static android.provider.MediaStore.Video.VideoColumns.DESCRIPTION;
import static com.example.consumerapp.db.DatabaseContract.NoteColumns.CONTENT_URI;

public class NoteAddUpdateActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtTitle, edtDescription;
    private Button btnSubmit;

    private boolean isEdit = false;
    private Note note;
    private int position;
    //private NoteHelper noteHelpe;
    private Uri uriWithId;

    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_POSITION = "extra_position";
    public static final int REQUEST_ADD = 100;
    public static final int RESULT_ADD = 101;
    public static final int REQUEST_UPDATE = 200;
    public static final int RESULT_UPDATE = 201;
    public static final int RESULT_DELETE = 301;
    private final int ALERT_DIALOG_CLOSE = 10;
    private final int ALERT_DIALOG_DELETE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_add_update);

        edtTitle = findViewById(R.id.edt_title);
        edtDescription = findViewById(R.id.edt_description);
        btnSubmit = findViewById(R.id.btn_submit);

        // noteHelper = NoteHelper.getInstance(getApplicationContext());
        //noteHelper.open();

        note = getIntent().getParcelableExtra(EXTRA_NOTE);
        if (note != null) {
            position = getIntent().getIntExtra(EXTRA_POSITION, 0);
            isEdit = true;
        } else {
            note = new Note();
        }

        String actionBarTitle;
        String btnTitle;

        if (isEdit) {
            //Uri yang di dapatkan disini akan digunakan untuk ambil data dari provider
            //content://com.example.mynotesapp/note/id
            uriWithId = Uri.parse(CONTENT_URI + "/" + note.getId());
            if (uriWithId != null) {
                Cursor cursor = getContentResolver().query(uriWithId, null, null, null, null);

                if (cursor != null) {
                    note = MappingHelper.mapCursorToObject(cursor);
                    cursor.close();
                }
            }

            actionBarTitle = "Ubah";
            btnTitle = "Update";

            if (note != null) {
                edtTitle.setText(note.getTitle());
                edtDescription.setText(note.getDescription());
            }
        } else {
            actionBarTitle = "Tambah";
            btnTitle = "Simpan";
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(actionBarTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnSubmit.setText(btnTitle);
        btnSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_submit) {
            String title = edtTitle.getText().toString().trim();
            String description = edtDescription.getText().toString().trim();

            if (TextUtils.isEmpty(title)) {
                edtTitle.setError("Field can not be blank");
                return;
            }
            /*note.setTitle(title);
            note.setDescription(description);

            Intent intent = new Intent();
            intent.putExtra(EXTRA_NOTE, note);
            intent.putExtra(EXTRA_POSITION, position);*/

            ContentValues values = new ContentValues();
            values.put(TITLE, title);
            values.put(DESCRIPTION, description);

            if (isEdit) { //update
                //Gunakan uriWithId untuk update
                //content://com.example.mynotesapp/note/id
                getContentResolver().update(uriWithId, values, null, null);
                Toast.makeText(this, "Satu item berhasil diedit", Toast.LENGTH_SHORT).show();
                finish();
                /*long result = noteHelper.update(String.valueOf(note.getId()),values);
                if (result > 0){
                    setResult(RESULT_UPDATE, intent);
                    finish();
                }else {
                    Toast.makeText(NoteAddUpdateActivity.this, "Gagal mengupdate data", Toast.LENGTH_SHORT).show();
                }*/
            } else { //add
                note.setDate(getCurrentDate());
                values.put(DATE, getCurrentDate());
                /*long result = noteHelper.insert(values);

                if (result > 0){
                    note.setId((int) result);
                    setResult(RESULT_ADD, intent);
                    finish();
                }else {
                    Toast.makeText(NoteAddUpdateActivity.this, "Gagal Menambah Data", Toast.LENGTH_SHORT).show();
                }*/

                //Gunakan uriWIthId untuk update
                //content://com.example.mynotesapp/note/id

                getContentResolver().insert(CONTENT_URI, values);
                Toast.makeText(this, "Satu item berhasil disimpan", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCurrentDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();

        return  dateFormat.format(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isEdit){
            getMenuInflater().inflate(R.menu.menu_form,menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                showAlertDialog(ALERT_DIALOG_DELETE);
                break;
            case android.R.id.home:
                showAlertDialog(ALERT_DIALOG_CLOSE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        showAlertDialog(ALERT_DIALOG_CLOSE);
    }

    /*
    Konfirmasi dialog sebelum proses batal atau hapus
    close = 10
    delete = 20
    */

    private void showAlertDialog(int type){
        final boolean isDialogCLose = type == ALERT_DIALOG_CLOSE;
        String dialogTitle, dialogMessage;

        if (isDialogCLose){
            dialogTitle = "Batal";
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?";
        }else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?";
            dialogTitle = "Hapus Note";
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle(dialogTitle);
        alertDialogBuilder
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (isDialogCLose){
                            finish();
                        }else {
                            /*long result = noteHelper.deleteById(String.valueOf(note.getId()));
                            if (result > 0){
                                Intent intent = new Intent();
                                intent.putExtra(EXTRA_POSITION,position);
                                setResult(RESULT_DELETE,intent);
                                finish();
                            }else {*/
                                //Toast.makeText(NoteAddUpdateActivity.this, "Gagal Menghapus Data", Toast.LENGTH_SHORT).show();
                                //Gunakan uriWithId untuk delete
                                //content://com.example.mynotesapp/note/id
                                getContentResolver().delete(uriWithId,null,null);
                                Toast.makeText(NoteAddUpdateActivity.this, "Satu item berhasil dihapus", Toast.LENGTH_SHORT).show();
                                finish();
                            //}
                        }
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}