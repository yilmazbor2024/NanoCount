package com.example.barkodm.ui.shelf

import android.os.Bundle
import androidx.navigation.NavArgs

class ShelfEditFragmentArgs(
    var shelfId: Int = 0,
    var locationId: Int = 0
) : NavArgs {

    companion object {
        @JvmStatic
        fun fromBundle(bundle: Bundle): ShelfEditFragmentArgs {
            val args = ShelfEditFragmentArgs()
            bundle.getInt("shelfId").let { if (it != 0) args.shelfId = it }
            bundle.getInt("locationId").let { if (it != 0) args.locationId = it }
            return args
        }
    }
} 