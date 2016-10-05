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
public class CommentDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = "CommentDialog";



    private static final String ARG_ITEMS   = "ITEMS";
    private static final String ARG_COMMENT = "COMMENT";



    private OnFragmentInteractionListener mListener = null;
    private ArrayList<Integer>            mItems    = null;
    private String                        mComment  = null;
    private ArrayList<String>             mComments = null;



    /**
     * Creates new instance of CommentDialog with pre-entered comment
     * @param items      indices in the list
     * @param comment    comment
     * @return CommentDialog instance
     */
    public static CommentDialog newInstance(ArrayList<Integer> items, String comment)
    {
        CommentDialog fragment = new CommentDialog();

        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_ITEMS,   items);
        args.putString(          ARG_COMMENT, comment);
        fragment.setArguments(args);

        return fragment;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mItems   = getArguments().getIntegerArrayList(ARG_ITEMS);
        mComment = getArguments().getString(          ARG_COMMENT);

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

                                if (!comment.equals(""))
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
                                dialog.dismiss();
                            }
                        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return dialog;
    }

    /**
     * Handler for comment entered event
     * @param comment    comment
     */
    public void onCommentEntered(String comment)
    {
        if (mListener != null)
        {
            mListener.onCommentEntered(mItems, comment);
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
     * Saves last entered comments
     */
    private void saveLastComments()
    {
        SharedPreferences prefs = getActivity().getSharedPreferences(ApplicationPreferences.COMMENTS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(ApplicationPreferences.LAST_COMMENTS, mComments.size());

        for (int i = 0; i < mComments.size(); ++i)
        {
            editor.putString(ApplicationPreferences.ONE_COMMENT + "_" + String.valueOf(i + 1), mComments.get(i));
        }

        editor.apply();
    }

    /**
     * Loads last entered comments
     */
    private void loadLastComments()
    {
        mComments = new ArrayList<>();

        SharedPreferences prefs = getActivity().getSharedPreferences(ApplicationPreferences.COMMENTS_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        int commentCount = prefs.getInt(ApplicationPreferences.LAST_COMMENTS, 0);

        for (int i = 0; i < commentCount; ++i)
        {
            String comment = prefs.getString(ApplicationPreferences.ONE_COMMENT + "_" + String.valueOf(i + 1),"");

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
    public interface OnFragmentInteractionListener
    {
        /**
         * Handler for comment entered event
         * @param items      indices in the list
         * @param comment    comment
         */
        void onCommentEntered(ArrayList<Integer> items, String comment);
    }
}
