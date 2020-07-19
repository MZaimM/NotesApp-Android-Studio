package com.example.consumerapp;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.consumerapp.adapter.NoteAdapter;
import com.example.consumerapp.entity.Note;
import com.example.consumerapp.helper.MappingHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.example.consumerapp.db.DatabaseContract.NoteColumns.CONTENT_URI;

public class MainActivity extends AppCompatActivity implements LoadNotesCallback {
    private ProgressBar progressBar;
    private RecyclerView rvNotes;
    private FloatingActionButton fabAdd;
    private NoteAdapter adapter;

    public static final String EXTRA_STATE ="extra_state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Cunsumer Notes");
        }

        progressBar = findViewById(R.id.progressbar);
        rvNotes = findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        rvNotes.setHasFixedSize(true);
        adapter = new NoteAdapter(this);
        rvNotes.setAdapter(adapter);

        fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteAddUpdateActivity.class);
                startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD);
            }
        });

        /*======================jika menggunakan content provider===================*/
        /*noteHelper = NoteHelper.getInstance(getApplicationContext());
        noteHelper.open();*/

        HandlerThread handlerThread = new HandlerThread("DataObserver");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());

        DataObserver myObserver = new DataObserver(handler, this);
        getContentResolver().registerContentObserver(CONTENT_URI,true, myObserver);



        if (savedInstanceState == null){
            //proses ambil data
            new LoadNotesAsync(/*noteHelper*/this,this).execute();
        }else {
            ArrayList<Note> list = savedInstanceState.getParcelableArrayList(EXTRA_STATE);
            if (list != null){
                adapter.setListNote(list);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_STATE,adapter.getListNote());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null){
            //Akan dipanggil jika request codenya ADD
            if (requestCode == NoteAddUpdateActivity.REQUEST_ADD){
                if (resultCode == NoteAddUpdateActivity.RESULT_ADD){
                    Note note = data.getParcelableExtra(NoteAddUpdateActivity.EXTRA_NOTE);

                    adapter.addItem(note);
                    rvNotes.smoothScrollToPosition(adapter.getItemCount()-1);
                    showSnackBarMessage("Satu item Berhasil ditambahkan");
                }
            }
            //Update dan Delete memiliki request code sama akan tetapi result codenya berbeda
            else if (requestCode == NoteAddUpdateActivity.REQUEST_UPDATE){

                if (resultCode == NoteAddUpdateActivity.RESULT_UPDATE){
                    Note note = data.getParcelableExtra(NoteAddUpdateActivity.EXTRA_NOTE);
                    int position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION,0);

                    adapter.updateItem(position,note);
                    rvNotes.smoothScrollToPosition(position);

                    showSnackBarMessage("Satu item berhasil diubah");
                }
                else if (resultCode == NoteAddUpdateActivity.RESULT_DELETE){
                    int position = data.getIntExtra(NoteAddUpdateActivity.EXTRA_POSITION,0);
                    adapter.removeItem(position);
                    showSnackBarMessage("Satu item berhasil dihapus");
                }
            }
        }
    }

    private void showSnackBarMessage(String message){
        Snackbar.make(rvNotes,message,Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //noteHelper.close();
    }

    @Override
    public void preExecute() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void postExecute(ArrayList<Note> notes) {
        progressBar.setVisibility(View.INVISIBLE);
        if (notes.size() > 0){
            adapter.setListNote(notes);
        }else {
            adapter.setListNote(new ArrayList<Note>());
            showSnackBarMessage("Tidak ada data saat ini");
        }
    }

    private static class LoadNotesAsync extends AsyncTask<Void,Void, ArrayList<Note>> {
        //private final WeakReference<NoteHelper> weakNoteContext;
        private final WeakReference<Context> weakContext;
        private final WeakReference<LoadNotesCallback> weakCallback;

        private LoadNotesAsync(Context context, LoadNotesCallback callback) {
            //weakNoteHelper = new WeakReference<>(noteHelper);
            weakContext = new WeakReference<>(context);
            weakCallback = new WeakReference<>(callback);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weakCallback.get().preExecute();
        }

        @Override
        protected ArrayList<Note> doInBackground(Void... voids) {
            //Cursor dataCursor = weakNoteHelper.get().queryAll();
            Context context = weakContext.get();
            Cursor dataCursor = context.getContentResolver().query(CONTENT_URI, null,null,null,null);
            return MappingHelper.mapCursorToArrayList(dataCursor);
        }


        @Override
        protected void onPostExecute(ArrayList<Note> notes) {
            super.onPostExecute(notes);
            weakCallback.get().postExecute(notes);
        }
    }

    public static class  DataObserver extends ContentObserver{

        final Context context;
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public DataObserver(Handler handler, Context context) {
            super(handler);
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            new LoadNotesAsync(context, (LoadNotesCallback) context).execute();
        }
    }
}

interface LoadNotesCallback{
    void preExecute();
    void postExecute(ArrayList<Note> notes);
}