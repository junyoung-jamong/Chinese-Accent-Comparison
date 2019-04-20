package com.smartjackwp.junyoung.cacp.CustomizedUI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FileDialog {
    private static final String PARENT_DIR = "..";
    private final String TAG = getClass().getName();
    private String[] fileList;
    private File currentPath;
    public interface FileSelectedListener {
        void fileSelected(File file);
    }
    public interface DirectorySelectedListener {
        void directorySelected(File directory);
    }
    private ListenerList<FileSelectedListener> fileListenerList = new ListenerList<FileDialog.FileSelectedListener>();
    private ListenerList<DirectorySelectedListener> dirListenerList = new ListenerList<FileDialog.DirectorySelectedListener>();
    private final Activity activity;
    private boolean selectDirectoryOption;
    private String[] fileEndsWith;
    private File initPath;

    /**
     * @param activity
     * @param initialPath
     */
    public FileDialog(Activity activity, File initialPath) {
        this(activity, initialPath, null);
    }

    public FileDialog(Activity activity, File initialPath, String[] fileEndsWith) {
        this.activity = activity;
        this.fileEndsWith = fileEndsWith;
        if (!initialPath.exists()) initialPath = Environment.getExternalStorageDirectory();
        initPath = initialPath;
        loadFileList(initialPath);
    }

    /**
     * @return file dialog
     */
    public Dialog createFileDialog() {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(currentPath.getPath());
        if (selectDirectoryOption) {
            builder.setPositiveButton("Select directory", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, currentPath.getPath());
                    fireDirectorySelectedEvent(currentPath);
                }
            });
        }

        builder.setItems(fileList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String fileChosen = fileList[which];
                File chosenFile = getChosenFile(fileChosen);
                if (chosenFile.isDirectory()) {
                    loadFileList(chosenFile);
                    dialog.cancel();
                    dialog.dismiss();
                    showDialog();
                } else fireFileSelectedEvent(chosenFile);
            }
        });

        dialog = builder.show();
        return dialog;
    }


    public void addFileListener(FileSelectedListener listener) {
        fileListenerList.add(listener);
    }

    public void removeFileListener(FileSelectedListener listener) {
        fileListenerList.remove(listener);
    }

    public void setSelectDirectoryOption(boolean selectDirectoryOption) {
        this.selectDirectoryOption = selectDirectoryOption;
    }

    public void addDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.add(listener);
    }

    public void removeDirectoryListener(DirectorySelectedListener listener) {
        dirListenerList.remove(listener);
    }

    /**
     * Show file dialog
     */
    public void showDialog() {
        createFileDialog().show();
    }

    private void fireFileSelectedEvent(final File file) {
        fileListenerList.fireEvent(new ListenerList.FireHandler<FileSelectedListener>() {
            public void fireEvent(FileSelectedListener listener) {
                listener.fileSelected(file);
            }
        });
    }

    private void fireDirectorySelectedEvent(final File directory) {
        dirListenerList.fireEvent(new ListenerList.FireHandler<DirectorySelectedListener>() {
            public void fireEvent(DirectorySelectedListener listener) {
                listener.directorySelected(directory);
            }
        });
    }

    private void loadFileList(File path) {
        this.currentPath = path;
        List<String> r = new ArrayList<>();
        if (path.exists()) {
            if (path.getParentFile() != null && !path.getAbsolutePath().equals(initPath.getAbsolutePath())) r.add(PARENT_DIR);

            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    if (!sel.canRead()) return false;
                    if (selectDirectoryOption) return sel.isDirectory() && containsSoundFile(sel);
                    else {
                        for(String format: fileEndsWith)
                            if(filename.toLowerCase().endsWith(format))
                                return true;
                        return containsSoundFile(sel) || false;
                    }
                }
            };
            String[] fileList1 = path.list(filter);
            for (String file : fileList1) {
                r.add(file);
            }
        }
        fileList = (String[]) r.toArray(new String[]{});

        //getFilesList(path);
    }

    private boolean containsSoundFile(File dir){
        Queue<File> dirQueue = new LinkedList<>();
        if(dir.isDirectory()){
           dirQueue.add(dir);

           while(!dirQueue.isEmpty()) {
               File currentDIR = dirQueue.remove();
               File[] fileList = currentDIR.listFiles();
               for(File f: fileList){
                   if(f.isDirectory())
                       dirQueue.add(f);
                   else {
                       for(String format: fileEndsWith)
                            if(f.getName().toLowerCase().endsWith(format))
                                return true;
                   }
               }
           }
        }
        return false;
    }

    public void getFilesList(File path) {
        File root;
        if(path == null)
            root  = Environment.getExternalStorageDirectory();
        else
            root = path;
        File[] fileList = root.listFiles();
        for(int i=0; i<fileList.length; i++){

            if(fileList[i].toString().contains(".mp3") || fileList[i].toString().contains(".wav"))
                Log.e("getFileList", fileList[i].toString());
        }
    }

    private File getChosenFile(String fileChosen) {
        if (fileChosen.equals(PARENT_DIR)) return currentPath.getParentFile();
        else return new File(currentPath, fileChosen);
    }
}

class ListenerList<L> {
    private List<L> listenerList = new ArrayList<L>();

    public interface FireHandler<L> {
        void fireEvent(L listener);
    }

    public void add(L listener) {
        listenerList.add(listener);
    }

    public void fireEvent(FireHandler<L> fireHandler) {
        List<L> copy = new ArrayList<L>(listenerList);
        for (L l : copy) {
            fireHandler.fireEvent(l);
        }
    }

    public void remove(L listener) {
        listenerList.remove(listener);
    }

    public List<L> getListenerList() {
        return listenerList;
    }
}
