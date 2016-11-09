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

import com.griscom.codereview.R;
import com.griscom.codereview.other.ApplicationPreferences;

import java.util.ArrayList;

/**
 * Dialog for creating comments
 */
@SuppressWarnings({"ClassWithoutConstructor", "PublicConstructor"})
public class CommentDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = "CommentDialog";



    private static final String ARG_FIRST_ROW = "FIRST_ROW";
    private static final String ARG_LAST_ROW  = "LAST_ROW";
    private static final String ARG_COMMENT   = "COMMENT";



    private OnFragmentInteractionListener mListener = null;
    private int                           mFirstRow = 0;
    private int                           mLastRow  = 0;
    private String                        mComment  = null;
    private ArrayList<String>             mComments = null;



    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "CommentDialog{" +
                "mListener="   + mListener +
                ", mFirstRow=" + mFirstRow +
                ", mLastRow="  + mLastRow  +
                ", mComment='" + mComment  + '\'' +
                ", mComments=" + mComments +
                '}';
    }

    /**
     * Creates new instance of CommentDialog with pre-entered comment
     * @param firstRow    first row
     * @param lastRow     last row
     * @param comment     comment
     * @return CommentDialog instance
     */
    public static CommentDialog newInstance(int firstRow, int lastRow, String comment)
    {
        CommentDialog fragment = new CommentDialog();

        Bundle args = new Bundle();
        args.putInt(   ARG_FIRST_ROW, firstRow);
        args.putInt(   ARG_LAST_ROW,  lastRow);
        args.putString(ARG_COMMENT,   comment);
        fragment.setArguments(args);

        return fragment;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mFirstRow = getArguments().getInt(   ARG_FIRST_ROW);
        mLastRow  = getArguments().getInt(   ARG_LAST_ROW);
        mComment  = getArguments().getString(ARG_COMMENT);

        loadLastComments();
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final AutoCompleteTextView editText = new AutoCompleteTextView(getContext());
        editText.setText(mComment);
        //noinspection deprecation
        editText.setTextColor(getResources().getColor(R.color.textColor));
        editText.selectAll();
        editText.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, mComments));
        editText.setThreshold(0);



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_comment_title)
                .setMessage(R.string.dialog_comment_message)
                .setCancelable(true)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                String comment = editText.getText().toString();

                                if (!comment.isEmpty())
                                {
                                    mComments.remove(comment);
                                    mComments.add(0, comment);

                                    saveLastComments();
                                }

                                onCommentEntered(comment);

                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                onCommentCanceled();

                                dialog.dismiss();
                            }
                        })
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener()
                        {
                            @Override
                            public void onCancel(DialogInterface dialog)
                            {
                                onCommentCanceled();
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);

        onCommentCanceled();
    }

    /**
     * Handler for comment entered event
     * @param comment    comment
     */
    private void onCommentEntered(String comment)
    {
        if (mListener != null)
        {
            mListener.onCommentEntered(mFirstRow, mLastRow, comment);
        }
    }

    /**
     * Handler for comment canceled event
     */
    private void onCommentCanceled()
    {
        if (mListener != null)
        {
            mListener.onCommentCanceled();
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
            //noinspection ProhibitedExceptionThrown
            throw new RuntimeException(context + " must implement OnFragmentInteractionListener");
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
     * Saves last entered comments
     */
    private void saveLastComments()
    {
        SharedPreferences prefs = getActivity().getSharedPreferences(ApplicationPreferences.COMMENTS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(ApplicationPreferences.LAST_COMMENTS, mComments.size());

        for (int i = 0; i < mComments.size(); ++i)
        {
            editor.putString(ApplicationPreferences.ONE_COMMENT + '_' + (i + 1), mComments.get(i));
        }

        editor.apply();
    }

    /**
     * Loads last entered comments
     */
    private void loadLastComments()
    {
        mComments = new ArrayList<>(0);

        SharedPreferences prefs = getActivity().getSharedPreferences(ApplicationPreferences.COMMENTS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        int commentCount = prefs.getInt(ApplicationPreferences.LAST_COMMENTS, 0);

        for (int i = 0; i < commentCount; ++i)
        {
            String comment = prefs.getString(ApplicationPreferences.ONE_COMMENT + '_' + (i + 1), "");

            if (
                !TextUtils.isEmpty(comment)
                &&
                !mComments.contains(comment)
               )
            {
                mComments.add(comment);
            }
        }
    }



    /**
     * Listener for interaction with this dialog fragment
     */
    @SuppressWarnings("PublicInnerClass")
    public interface OnFragmentInteractionListener
    {
        /**
         * Handler for comment entered event
         * @param firstRow    first row
         * @param lastRow     last row
         * @param comment     comment
         */
        void onCommentEntered(int firstRow, int lastRow, String comment);

        /**
         * Handler for comment canceled event
         */
        void onCommentCanceled();
    }
}
