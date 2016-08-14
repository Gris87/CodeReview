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
import com.griscom.codereview.other.SortType;

/**
 * Dialog for selecting sort type
 */
public class SortDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = "SortDialog";



    private static final String ARG_SORT_TYPE = "SORT_TYPE";



    private OnFragmentInteractionListener mListener = null;
    private int                           mSortType = SortType.NONE;



    /**
     * Creates new instance of SortDialog with pre-selected sort type
     * @param sortType  sort type
     * @return SortDialog instance
     */
    public static SortDialog newInstance(int sortType)
    {
        SortDialog fragment = new SortDialog();

        Bundle args = new Bundle();
        args.putInt(ARG_SORT_TYPE, sortType);
        fragment.setArguments(args);

        return fragment;
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mSortType = getArguments().getInt(ARG_SORT_TYPE);
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.action_sort)
                .setCancelable(true)
                .setSingleChoiceItems(R.array.sort_types,
                        mSortType - 1,
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
                                onSortTypeSelected(which + 1);

                                dialog.dismiss();
                            }
                        });

        return builder.create();
    }

    /**
     * Handler for sort type selected event
     * @param sortType    selected sort type
     */
    public void onSortTypeSelected(int sortType)
    {
        if (mListener != null)
        {
            mListener.onSortTypeSelected(sortType);
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
         * Handler for sort type selected event
         * @param sortType    selected sort type
         */
        void onSortTypeSelected(int sortType);
    }
}
