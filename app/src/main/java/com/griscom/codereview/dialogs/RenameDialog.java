package com.griscom.codereview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.griscom.codereview.R;
import com.griscom.codereview.other.ApplicationPreferences;

import java.util.ArrayList;

/**
 * Dialog for renaming file
 */
public class RenameDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = "RenameDialog";



    private static final String ARG_ADD_NOTE  = "ADD_NOTE";
    private static final String ARG_ITEM      = "ITEM";
    private static final String ARG_FILE_NAME = "FILE_NAME";



    private OnFragmentInteractionListener mListener  = null;
    private boolean                       mAddNote   = false;
    private int                           mItem      = 0;
    private String                        mFileName  = null;
    private ArrayList<String>             mFileNames = null;



    /**
     * Creates new instance of RenameDialog with pre-entered file name
     * @param addNote   true, if need to add note for renaming
     * @param item      file index in the list
     * @param fileName  file name
     * @return RenameDialog instance
     */
    public static RenameDialog newInstance(boolean addNote, int item, String fileName)
    {
        RenameDialog fragment = new RenameDialog();

        Bundle args = new Bundle();
        args.putBoolean(ARG_ADD_NOTE,  addNote);
        args.putInt(    ARG_ITEM,      item);
        args.putString( ARG_FILE_NAME, fileName);
        fragment.setArguments(args);

        return fragment;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mAddNote  = getArguments().getBoolean(ARG_ADD_NOTE);
        mItem     = getArguments().getInt(    ARG_ITEM);
        mFileName = getArguments().getString( ARG_FILE_NAME);

        loadLastFileNames();
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final AutoCompleteTextView editText = new AutoCompleteTextView(getContext());
        editText.setText(mFileName);
        //noinspection deprecation
        editText.setTextColor(getResources().getColor(R.color.textColor));
        editText.selectAll();
        editText.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, mFileNames));
        editText.setThreshold(0);



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_rename_title)
                .setMessage(R.string.dialog_rename_message)
                .setCancelable(true)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                String fileName = editText.getText().toString();

                                if (!fileName.equals(""))
                                {
                                    mFileNames.remove(fileName);
                                    mFileNames.add(0, fileName);
                                    saveLastFileNames();

                                    onFileRenamed(fileName);

                                    dialog.dismiss();
                                }
                                else
                                {
                                    Toast.makeText(getActivity(), R.string.dialog_rename_empty_name, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                dialog.dismiss();
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return dialog;
    }

    /**
     * Handler for file rename event
     * @param fileName    file name
     */
    public void onFileRenamed(String fileName)
    {
        if (mListener != null)
        {
            mListener.onFileRenamed(mAddNote, mItem, fileName);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener)context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onDetach()
    {
        super.onDetach();

        mListener = null;
    }

    /**
     * Saves last entered file names
     */
    private void saveLastFileNames()
    {
        SharedPreferences prefs = getActivity().getSharedPreferences(ApplicationPreferences.FILE_NAMES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(ApplicationPreferences.LAST_FILENAMES, mFileNames.size());

        for (int i = 0; i < mFileNames.size(); ++i)
        {
            editor.putString(ApplicationPreferences.ONE_FILENAME + "_" + String.valueOf(i + 1), mFileNames.get(i));
        }

        editor.apply();
    }

    /**
     * Loads last entered file names
     */
    private void loadLastFileNames()
    {
        mFileNames = new ArrayList<>();

        SharedPreferences prefs = getActivity().getSharedPreferences(ApplicationPreferences.FILE_NAMES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        int fileNameCount = prefs.getInt(ApplicationPreferences.LAST_FILENAMES, 0);

        for (int i = 0; i < fileNameCount; ++i)
        {
            String fileName = prefs.getString(ApplicationPreferences.ONE_FILENAME + "_" + String.valueOf(i + 1),"");

            if (
                !TextUtils.isEmpty(fileName)
                &&
                !mFileNames.contains(fileName)
               )
            {
                mFileNames.add(fileName);
            }
        }
    }



    /**
     * Listener for interaction with this dialog fragment
     */
    public interface OnFragmentInteractionListener
    {
        /**
         * Handler for file rename event
         * @param addNote     true, if need to add note for renaming
         * @param item        file index in the list
         * @param fileName    file name
         */
        void onFileRenamed(boolean addNote, int item, String fileName);
    }
}
