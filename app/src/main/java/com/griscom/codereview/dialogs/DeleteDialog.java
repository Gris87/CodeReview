package com.griscom.codereview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.griscom.codereview.R;

/**
 * Dialog for confirming deletion
 */
public class DeleteDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = "DeleteDialog";



    private static final String ARG_ITEMS         = "ITEMS";
    private static final String ARG_FOLDERS_COUNT = "FOLDERS_COUNT";
    private static final String ARG_FILES_COUNT   = "FILES_COUNT";



    private OnFragmentInteractionListener mListener     = null;
    private int[]                         mItems        = null;
    private int                           mFoldersCount = 0;
    private int                           mFilesCount   = 0;



    /**
     * Creates new instance of DeleteDialog
     * @param items           file indices in the list
     * @param foldersCount    amount of folders
     * @param filesCount      amount of files
     * @return DeleteDialog instance
     */
    public static DeleteDialog newInstance(int[] items, int foldersCount, int filesCount)
    {
        DeleteDialog fragment = new DeleteDialog();

        Bundle args = new Bundle();
        args.putIntArray(ARG_ITEMS,         items);
        args.putInt(     ARG_FOLDERS_COUNT, foldersCount);
        args.putInt(     ARG_FILES_COUNT,   filesCount);
        fragment.setArguments(args);

        return fragment;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mItems        = getArguments().getIntArray(ARG_ITEMS);
        mFoldersCount = getArguments().getInt(     ARG_FOLDERS_COUNT);
        mFilesCount   = getArguments().getInt(     ARG_FILES_COUNT);
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Resources resources = getResources();

        String folders = mFoldersCount > 0 ? resources.getQuantityString(R.plurals.delete_folders_plurals, mFoldersCount, mFoldersCount) : null;
        String files   = mFilesCount   > 0 ? resources.getQuantityString(R.plurals.delete_files_plurals,   mFilesCount,   mFilesCount)   : null;

        String message;

        if (mFoldersCount > 0 && mFilesCount == 0)
        {
            message = resources.getString(R.string.dialog_delete_folders_or_files_message, folders);
        }
        else
        if (mFoldersCount == 0 && mFilesCount > 0)
        {
            message = resources.getString(R.string.dialog_delete_folders_or_files_message, files);
        }
        else
        {
            message = resources.getString(R.string.dialog_delete_folders_and_files_message, folders, files);
        }



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_delete_title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int index)
                    {
                        onDeleteConfirmed();

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
     * Handler for delete confirmed event
     */
    public void onDeleteConfirmed()
    {
        if (mListener != null)
        {
            mListener.onDeleteConfirmed(mItems);
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
         * Handler for delete confirmed event
         * @param items    file indices in the list
         */
        void onDeleteConfirmed(int[] items);
    }
}