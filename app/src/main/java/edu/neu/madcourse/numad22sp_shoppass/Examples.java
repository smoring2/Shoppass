package edu.neu.madcourse.numad22sp_shoppass;

import org.json.JSONException;

import edu.neu.madcourse.numad22sp_shoppass.components.Product;
import edu.neu.madcourse.numad22sp_shoppass.components.Promotion;
import edu.neu.madcourse.numad22sp_shoppass.components.Store;

public class Examples {
    public static Store store1 = new Store("Jack in the Box", "911 El Camino Real, Santa Clara, CA 95050");
    public static Store store2 = new Store("McDonald's", "1451 Coleman Ave, Santa Clara, CA 95050");
    public static Store store3 = new Store("Chungdam", "3180 El Camino Real, Santa Clara, CA 95051");


    public static Store favStore1 = new Store("favStore1", "favAdd1");
    public static Store favStore2 = new Store("favStore2", "favAdd2");
    public static Store favStore3 = new Store("favStore3", "favAdd3");


    public static Promotion promotion1 = new Promotion("", "", "promotion1", "promotion1 desc");
    public static Promotion promotion2 = new Promotion("", "", "promotion2", "promotion2 desc");
    public static Promotion promotion3 = new Promotion("", "", "promotion3", "promotion3 desc");



    public static int PRODUCT_IMG = R.drawable.example_product_img;
    public static int STORE_IMG = R.drawable.example_store_img;

    public static int PORD_IMG_1 = R.drawable.example_product_img;
    public static int PORD_IMG_2 = R.drawable.icons_heart_48;
    public static int PORD_IMG_3 = R.drawable.icons_unlikeheart_30;

    public static int ARROW_RIGHT = R.drawable.icons_sortright_30;
    public static int ARROW_DOWN = R.drawable.icons_sordown_30;

    public static int ADD_PRODUCT_IMG = R.drawable.icons_add_product_66;
    public static int ADD_PROMOTION_IMG = R.drawable.icons_add_50;





}
