package com.pay2.exhangeapp.common

import java.text.DecimalFormat

fun Double.formatDecimal(): String {
    return DecimalFormat("###,###.###").format(this)
}