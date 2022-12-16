package com.buscalibre.app2.constants;

public class ServerConstants {

    static public int NO_ERROR                                 =  0;
    static public int AUTH_TOKEN_MISSING                       = -1;
    static public int AUTH_TOKEN_EXPIRED                       = -2;
    static public int USER_NOT_FOUND                           = -3;
    static public int INVALID_MESSAGE                          = -4;
    static public int USER_LOGIN                               = -5;
    static public int MC_ERROR                                 = -6;
    static public int REGISTER_ERROR                           = -7;
    static public  int AUTH_TOKEN_ERROR                        =-21;
    static public int  MAIN_MENU_TYPE_WEBVIEW                  = 1; // WebView
    static public int MAIN_MENU_TYPE_SEARCH_ISBN               = 2; // Escanear código del libro o ISBN
    static public int MAIN_MENU_TYPE_SEARCH_KEY_WORDS          = 3; // Buscar por Título o autor del libro
    static public int MAIN_MENU_TYPE_EXTERNAL_STORE            = 4; // Te Traemos
    static public int MAIN_MENU_TYPE_SELL_BOOKS                = 6; // Vneder libros

    static public int SELLER_PRODUCT_TYPE_BOOK                 = 1;
    static public int SELLER_PRODUCT_STATUS_PUBLISHED          = 2;
    static public int SELLER_PAYMENT_STATUS_PENDING                 = 1;
    static public int SELLER_PAYMENT_STATUS_PAYED          = 2;
    static public int MAIN_MENU_TYPE_EBOOK_READER              = 5;
    static public int EBOOK_TYPE_EBOOK                         = 1;
    static public int EBOOK_TYPE_AUDIO                         = 2;
    static public int NO_PENDINGS_PAYMENTS                                 =  -50;
    static public int NO_QUOTE_ISBN                                 =  -47;



}
