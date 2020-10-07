package com.gochiusa.musicapp.plus.entity

import com.gochiusa.musicapp.library.update.UpdateVersionJson

data class UpdateInformation(var versionCode: Int?,
                             var versionName: String?,
                             var applicationId: String?,
                             var variantName: String?,
                             var outputFile: String?,
                             var versionDescription: String?) {
    constructor(updateVersionJson: UpdateVersionJson): this(updateVersionJson.versionCode,
        updateVersionJson.versionName, updateVersionJson.applicationId,
        updateVersionJson.variantName, updateVersionJson.outputFile,
        updateVersionJson.versionDescription)
}