package com.pay2.exhangeapp.common


object DatabaseConst {
    object Entity {
        const val CURRENCY_ENTITY = "currency"
        const val EXCHANGE_RATES_ENTITY = "exchange_rates"
        const val REFRESH_SCHEDULES_ENTITY = "refresh_schedules"
    }

    const val EXCHANGE_APP_DB = "exchange_app_db"
}

object NetworkConst {
    object EndPoints {
        const val CURRENCIES = "currencies.json"
        const val EXCHANGE_RATES = "latest.json"
    }

    const val READ_TIMEOUT = 60L
    const val CONNECT_TIMEOUT = 60L
    const val WRITE_TIMEOUT = 60L
    const val APP_ID_QUERY_PARAM = "app_id"
    const val REFRESH_TIMEOUT = 30L
}

object AppConst {
    const val SHARED_PREF_NAME = "exchange_app_shared_pref"
}