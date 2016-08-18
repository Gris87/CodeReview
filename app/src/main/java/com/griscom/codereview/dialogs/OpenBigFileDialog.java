package com.griscom.codereview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.griscom.codereview.R;

/**
 * Dialog for asking opening big file
 */
public class OpenBigFileDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = "OpenBigFileDialog";



    private static final String ARG_FILE_PATH = "FILE_PATH";
    private static final String ARG_FILE_ID   = "FILE_ID";



    private OnFragmentInteractionListener mListener = null;
    private String                        mFilePath = null;
    private int                           mFileId   = 0;



    /**
     * Creates new instance of OpenBigFileDialog
     * @param filePath    path to file
     * @param fileId      file ID in DB
     * @return OpenBigFileDialog instance
     */
    public static OpenBigFileDialog newInstance(String filePath, int fileId)
    {
        OpenBigFileDialog fragment = new OpenBigFileDialog();

        Bundle args = new Bundle();
        args.putString(ARG_FILE_PATH, filePath);
        args.putInt(   ARG_FILE_ID,   fileId);
        fragment.setArguments(args);

        return fragment;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mFilePath = getArguments().getString(ARG_FILE_PATH);
        mFileId   = getArguments().getInt(   ARG_FILE_ID);
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_open_big_file_title)
                .setMessage(getString(R.string.dialog_open_big_file_message, mFilePath.substring(mFilePath.lastIndexOf('/') + 1)))
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int index)
                    {
                        onBigFileOpeningConfirmed();

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int index)
                    {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    /**
     * Handler for big file opening confirmed event
     */
    public void onBigFileOpeningConfirmed()
    {
        if (mListener != null)
        {
            mListener.onBigFileOpeningConfirmed(mFilePath, mFileId);
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
     * Listener for interaction with this dialog fragment
     */
    public interface OnFragmentInteractionListener
    {
        /**
         * Handler for big file opening confirmed event
         * @param filePath    path to file
         * @param fileId      file ID in DB
         */
        void onBigFileOpeningConfirmed(String filePath, int fileId);
    }
}
