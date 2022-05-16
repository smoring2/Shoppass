package edu.neu.madcourse.numad22sp_shoppass.activities;


import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import edu.neu.madcourse.numad22sp_shoppass.R;
import edu.neu.madcourse.numad22sp_shoppass.activities.recyclerview.BuyerHomeFavStoresAdapter;

public class BuyerHomeActivityTest {
    @Rule
    public ActivityTestRule<BuyerHomeActivity> buyerHomeActivityActivityTestRuler
            = new ActivityTestRule<>(BuyerHomeActivity.class);


    @Test
    public void onCreate() {
    }

    @Test
    public void createRvFavStores() {
//        Espresso.onView((withId(R.id.rv_favStores))).perform(RecyclerViewActions.<BuyerHomeFavStoresAdapter.SellerHomeFavStoresViewHolder>actionOnItemAtPosition (0, click()));
        RecyclerView favStores = buyerHomeActivityActivityTestRuler.getActivity().findViewById(R.id.rv_favStores);
        int itemcount = favStores.getAdapter().getItemCount();
        if (itemcount >  0) {
            Espresso.onView((withId(R.id.rv_favStores))).perform(RecyclerViewActions.scrollToPosition(0));
        }
    }

    @Test
    public void createRvPromotions() {
    }

    @Test
    public void createRvAllShops() {
    }
}