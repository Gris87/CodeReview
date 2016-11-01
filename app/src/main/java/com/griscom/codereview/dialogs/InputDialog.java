package com.griscom.codereview.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.WindowManager;
import android.widget.EditText;

import com.griscom.codereview.R;

/**
 * Dialog for inputting text
 */
public class InputDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = "InputDialog";



    private static final String ARG_ID      = "ID";
    private static final String ARG_TITLE   = "TITLE";
    private static final String ARG_MESSAGE = "MESSAGE";
    private static final String ARG_TEXT    = "TEXT";
    private static final String ARG_DATA    = "DATA";



    private OnFragmentInteractionListener mListener = null;
    private int                           mId       = 0;
    private int                           mTitle    = 0;
    private int                           mMessage  = 0;
    private String                        mText     = null;
    private Bundle                        mData     = null;



    /**
     * Creates new instance of InputDialog with pre-entered text
     * @param id         InputDialog ID
     * @param title      title resource ID
     * @param message    message resource ID
     * @param text       text
     * @param data       additional data
     * @return InputDialog instance
     */
    public static InputDialog newInstance(int id, int title, int message, String text, Bundle data)
    {
        InputDialog fragment = new InputDialog();

        Bundle args = new Bundle();
        args.putInt(   ARG_ID,      id);
        args.putInt(   ARG_TITLE,   title);
        args.putInt(   ARG_MESSAGE, message);
        args.putString(ARG_TEXT,    text);
        args.putBundle(ARG_DATA,    data);
        fragment.setArguments(args);

        return fragment;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mId      = getArguments().getInt(   ARG_ID);
        mTitle   = getArguments().getInt(   ARG_TITLE);
        mMessage = getArguments().getInt(   ARG_MESSAGE);
        mText    = getArguments().getString(ARG_TEXT);
        mData    = getArguments().getBundle(ARG_DATA);
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final EditText editText = new EditText(getContext());
        editText.setText(mText);
        //noinspection deprecation
        editText.setTextColor(getResources().getColor(R.color.textColor));
        editText.selectAll();



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(mTitle)
                .setMessage(mMessage)
                .setCancelable(true)
                .setView(editText)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener()
                        {
                            /**
                             * Handler for click event
                             *
                             * @param dialog        Dialog
                             * @param whichButton   Selected option
                             */
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                onTextEntered(editText.getText().toString());

                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener()
                        {
                            /**
                             * Handler for click event
                             *
                             * @param dialog        Dialog
                             * @param whichButton   Selected option
                             */
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
     * Handler for text entered event
     * @param text    text
     */
    private void onTextEntered(String text)
    {
        if (mListener != null)
        {
            mListener.onTextEntered(mId, text, mData);
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
         * Handler for text entered event
         * @param id      InputDialog ID
         * @param text    text
         * @param data    additional data
         */
        void onTextEntered(int id, String text, Bundle data);
    }
}
