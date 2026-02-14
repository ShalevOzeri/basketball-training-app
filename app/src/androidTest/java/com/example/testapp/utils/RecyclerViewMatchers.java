package com.example.testapp.utils;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Custom Espresso Matchers for RecyclerView testing
 * 
 * Allow tests on specific elements within RecyclerView
 */
public class RecyclerViewMatchers {

    /**
     * Check RecyclerView size
     * @param expectedSize expected size
     */
    public static Matcher<View> withRecyclerViewSize(final int expectedSize) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            protected boolean matchesSafely(RecyclerView recyclerView) {
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                return adapter != null && adapter.getItemCount() == expectedSize;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("RecyclerView should have " + expectedSize + " items");
            }
        };
    }

    /**
     * Check that RecyclerView is not empty
     */
    public static Matcher<View> hasMinimumItems(final int minimumItems) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            protected boolean matchesSafely(RecyclerView recyclerView) {
                RecyclerView.Adapter adapter = recyclerView.getAdapter();
                return adapter != null && adapter.getItemCount() >= minimumItems;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("RecyclerView should have at least " + minimumItems + " items");
            }
        };
    }

    /**
     * Check that RecyclerView is empty
     */
    public static Matcher<View> isEmpty() {
        return withRecyclerViewSize(0);
    }
}
