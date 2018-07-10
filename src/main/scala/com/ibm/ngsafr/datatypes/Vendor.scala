package com.ibm.ngsafr.datatypes

case class Vendor(// Element      Data Type                Default Value       Description
             instinstID: String, // "0",               Instrument ID
             instEffectDate: String, // "0000-00-00",      Record Effective Start Date
             instEffectTime: String, // "0",               Record Effective Start Time
             instEffectEndDate: String, // "9999-99-99",      Record Effective End Date
             instEffectEndTime: String, // "9",               Record Effective End Time
             instHolderName: String, // "Vendor Name",     Name of Instrument Holder
             instTypeID: String, // "EXP",             Vendor (EXP) or REV Record
             instAuditTrail: String)                 // "0,                Timestamp Updated



