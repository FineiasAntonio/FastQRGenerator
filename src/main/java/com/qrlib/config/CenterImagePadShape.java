package com.qrlib.config;

/**
 * Shape of the background-colored pad painted behind the center image.
 */
public enum CenterImagePadShape {

    /** Rectangle following the image bounds. */
    SQUARE,

    /** Rectangle with rounded corners, matching the rounded-module aesthetic. */
    ROUNDED,

    /** Circle. The center image is cropped to a circle to fit inside it. */
    CIRCLE
}
