package com.buscalibre.app2.widgets;

import android.content.Context;
import android.content.res.TypedArray;

import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.buscalibre.app2.R;
import com.buscalibre.app2.models.Quotation;
import com.buscalibre.app2.util.GraphicUtils;

public class PriceView extends LinearLayout {

    /**
     * Used as placeholder when there is no min or max arrival time
     */
    private static final int DEFAULT_ARRIVAL_TIME = -1;

    // UI elements
    private TextView mPrice;
    private TextView mDeliveryTimeRange;
    private TextView mUnavailable;

    // Data for determining which shipping this price corresponds to
    private String mCondition;
    private String mShippingMethodKind;

    public PriceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Obtain the attributes
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.PriceView, 0, 0);
        boolean available = attributes.getBoolean(R.styleable.PriceView_available, false);
        String price = attributes.getString(R.styleable.PriceView_price);
        int minArrivalTime = attributes.getInt(R.styleable.PriceView_minArrivalTime, DEFAULT_ARRIVAL_TIME);
        int maxArrivalTime = attributes.getInt(R.styleable.PriceView_maxArrivalTime, DEFAULT_ARRIVAL_TIME);
        boolean selected = attributes.getBoolean(R.styleable.PriceView_selected, false);
        attributes.recycle();

        // Configure the root view
        setOrientation(LinearLayout.VERTICAL);
        int paddingPxs = GraphicUtils.dpToPx(context, 10);
        setPadding(paddingPxs, paddingPxs, paddingPxs, paddingPxs);
        setGravity(Gravity.CENTER);
        setClickable(true);

        // Inflate the layout
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_priceview, this, true);
        mPrice = (TextView) view.findViewById(R.id.tv_priceview_price);
        mDeliveryTimeRange = (TextView) view.findViewById(R.id.tv_priceview_delivery_time_range);
        mUnavailable = (TextView) view.findViewById(R.id.tv_priceview_unavailable);

        // Set up the view
        setUp(context, available, price, minArrivalTime, maxArrivalTime, null, null, selected);
    }

    public PriceView(Context context) {
        this(context, null);
    }

    /**
     * Resets the display of this view to its default state, removing any attached {@link View.OnClickListener}
     */
    public void reset() {
        mUnavailable.setVisibility(VISIBLE);
        mPrice.setText(null);
        mPrice.setVisibility(GONE);
        mDeliveryTimeRange.setText("");
        mDeliveryTimeRange.setVisibility(GONE);
        setBackground(null);
        setOnClickListener(null);
        mCondition = null;
        mShippingMethodKind = null;
    }

    /**
     * Set ups how this view is displayed
     *
     * @param context            Context used to obtain the resources needed for this view
     * @param available          Indicates if this price is available
     * @param price              Price to show
     * @param minArrivalTime     Minimum arrival time to show
     * @param maxArrivalTime     Maximum arrival time to show
     * @param selected           Indicates if this view is currently selected
     */
    public void setUp(Context context, boolean available, String price, int minArrivalTime,
                      int maxArrivalTime, String condition, String shippingMethodKind, boolean selected) {
        if (available && price != null && minArrivalTime != DEFAULT_ARRIVAL_TIME && maxArrivalTime != DEFAULT_ARRIVAL_TIME) {
            mUnavailable.setVisibility(GONE);

            mPrice.setText(price);
            mPrice.setVisibility(VISIBLE);

            String days = context.getString(R.string.priceview_days);
            mDeliveryTimeRange.setText("(" + minArrivalTime + " - " + maxArrivalTime + " " + days + ")");
            mDeliveryTimeRange.setVisibility(VISIBLE);

            mCondition = condition;
            mShippingMethodKind = shippingMethodKind;

            // Show the border, if selected
            if (selected) {
                select();
            }
        } else {
            reset();
        }
    }

    /**
     * Marks this {@link PriceView} as selected, updating its UI accordingly
     */
    public void select() {
        setBackground(ContextCompat.getDrawable(getContext(), R.drawable.price_border_selected));
    }

    /**
     * Disables this {@link PriceView} selection (if any), updating its UI accordingly
     */
    public void deselect() {
        setBackground(null);
    }

    /**
     * Returns true if this {@link PriceView} is selected; false otherwise
     */
    public boolean isSelected() {
        return getBackground() != null;
    }

    /**
     * Returns true if this {@link PriceView} is available; false otherwise
     */
    public boolean isAvailable() {
        return mUnavailable.getVisibility() != VISIBLE;
    }

    /**
     * Returns a string representing the condition of the price
     */
    public String getCondition() {
        return mCondition;
    }

    /**
     * Returns a string representing the shipping method kind of the price
     */
    public String getShippingMethodKind() {
        return mShippingMethodKind;
    }
}
