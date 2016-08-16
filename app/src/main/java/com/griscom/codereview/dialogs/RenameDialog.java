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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.griscom.codereview.R;
import com.griscom.codereview.other.ApplicationPreferences;

import java.util.ArrayList;

/**
 * Dialog for renaming file
 */
public class RenameDialog extends DialogFragment implements View.OnClickListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "RenameDialog";



    private static final String ARG_MARK      = "MARK";
    private static final String ARG_ITEM      = "ITEM";
    private static final String ARG_FILE_NAME = "FILE_NAME";



    private OnFragmentInteractionListener mListener      = null;
    private EditText                      mInputEditText = null;
    private ImageButton                   mChooseButton  = null;
    private boolean                       mMark          = false;
    private int                           mItem          = 0;
    private String                        mFileName      = null;
    private ArrayList<CharSequence>       mFileNames     = null;



    /**
     * Creates new instance of RenameDialog with pre-entered file name
     * @param mark      true, if we need to mark this file for renaming
     * @param item      file index in the list
     * @param fileName  file name
     * @return RenameDialog instance
     */
    public static RenameDialog newInstance(boolean mark, int item, String fileName)
    {
        RenameDialog fragment = new RenameDialog();

        Bundle args = new Bundle();
        args.putBoolean(ARG_MARK,      mark);
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

        mMark     = getArguments().getBoolean(ARG_MARK);
        mItem     = getArguments().getInt(    ARG_ITEM);
        mFileName = getArguments().getString( ARG_FILE_NAME);

        loadLastFileNames();
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View rootView = inflater.inflate(R.layout.dialog_input, null, false);



        mInputEditText = (EditText)   rootView.findViewById(R.id.inputEditText);
        mChooseButton  = (ImageButton)rootView.findViewById(R.id.chooseButton);



        mInputEditText.setText(mFileName);
        mInputEditText.selectAll();

        mChooseButton.setOnClickListener(this);



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_rename_title)
                .setMessage(R.string.dialog_rename_message)
                .setCancelable(true)
                .setView(rootView)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                String fileName = mInputEditText.getText().toString();

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
                                    Toast.makeText(getActivity(), R.string.empty_name, Toast.LENGTH_SHORT).show();
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

    /** {@inheritDoc} */
    @Override
    public void onClick(View view)
    {
        if (view == mChooseButton)
        {
            if (mFileNames.size() > 0)
            {
                final CharSequence items[] = new CharSequence[mFileNames.size()];
                String currentFilename = mInputEditText.getText().toString();
                int index = -1;

                for (int i = 0; i < mFileNames.size(); ++i)
                {
                    String oneFilename = (String)mFileNames.get(i);

                    if (index < 0 && oneFilename.equals(currentFilename))
                    {
                        index = i;
                    }

                    items[i] = oneFilename;
                }

                if (index < 0)
                {
                    index = 0;
                }

                AlertDialog chooseDialog = new AlertDialog.Builder(getActivity())
                        .setSingleChoiceItems(items, index, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int index)
                            {
                                mInputEditText.setText(items[index]);

                                dialog.dismiss();
                            }
                        }).create();

                chooseDialog.show();
            }
            else
            {
                Toast.makeText(getActivity(), R.string.no_last_names, Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Log.e(TAG, "Unknown view: " + String.valueOf(view));
        }
    }

    /**
     * Handler for file rename event
     * @param fileName    file name
     */
    public void onFileRenamed(String fileName)
    {
        if (mListener != null)
        {
            mListener.onFileRenamed(mMark, mItem, fileName);
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
    public void saveLastFileNames()
    {
        SharedPreferences prefs = getActivity().getSharedPreferences(ApplicationPreferences.FILE_NAMES_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(ApplicationPreferences.LAST_FILENAMES, mFileNames.size());

        for (int i = 0; i < mFileNames.size(); ++i)
        {
            editor.putString(ApplicationPreferences.ONE_FILENAME + "_" + String.valueOf(i + 1), mFileNames.get(i).toString());
        }

        editor.apply();
    }

    /**
     * Loads last entered file names
     */
    public void loadLastFileNames()
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
         * @param mark        true, if we need to mark this file for renaming
         * @param item        file index in the list
         * @param fileName    file name
         */
        void onFileRenamed(boolean mark, int item, String fileName);
    }
}
