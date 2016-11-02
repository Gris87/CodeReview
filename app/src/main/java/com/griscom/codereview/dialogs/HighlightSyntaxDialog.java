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
import com.griscom.codereview.other.SyntaxParserType;

/**
 * Dialog for selecting syntax parser type
 */
@SuppressWarnings({"ClassWithoutConstructor", "PublicConstructor"})
public class HighlightSyntaxDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = "HighlightSyntaxDialog";



    private static final String ARG_SYNTAX_PARSER_TYPE = "SYNTAX_PARSER_TYPE";



    private OnFragmentInteractionListener mListener         = null;
    private int                           mSyntaxParserType = SyntaxParserType.AUTOMATIC;



    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HighlightSyntaxDialog{" +
                "mListener=" + mListener +
                ", mSyntaxParserType=" + mSyntaxParserType +
                '}';
    }

    /**
     * Creates new instance of HighlightSyntaxDialog with pre-selected syntax parser type
     * @param syntaxParserType    syntax parser type
     * @return HighlightSyntaxDialog instance
     */
    public static HighlightSyntaxDialog newInstance(int syntaxParserType)
    {
        HighlightSyntaxDialog fragment = new HighlightSyntaxDialog();

        Bundle args = new Bundle();
        args.putInt(ARG_SYNTAX_PARSER_TYPE, syntaxParserType);
        fragment.setArguments(args);

        return fragment;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mSyntaxParserType = getArguments().getInt(ARG_SYNTAX_PARSER_TYPE);
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_highlight_syntax_title)
                .setCancelable(true)
                .setSingleChoiceItems(R.array.dialog_highlight_syntax_types,
                        mSyntaxParserType,
                        new DialogInterface.OnClickListener()
                        {
                            /**
                             * Handler for click event
                             *
                             * @param dialog    Dialog
                             * @param which     Selected option
                             */
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                onSyntaxParserTypeSelected(which);

                                dialog.dismiss();
                            }
                        });

        return builder.create();
    }

    /**
     * Handler for syntax parser type selected event
     * @param syntaxParserType    selected syntax parser type
     */
    private void onSyntaxParserTypeSelected(int syntaxParserType)
    {
        if (mListener != null)
        {
            mListener.onSyntaxParserTypeSelected(syntaxParserType);
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
     * Listener for interaction with this dialog fragment
     */
    public interface OnFragmentInteractionListener
    {
        /**
         * Handler for syntax parser type selected event
         * @param syntaxParserType    selected syntax parser type
         */
        void onSyntaxParserTypeSelected(int syntaxParserType);
    }
}
