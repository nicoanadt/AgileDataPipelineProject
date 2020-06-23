package uk.ac.man.cs.agiledata

class ConfigSchemaTuples(paramSchema: Array[(String,String)]) {
  def getConfigSchema(): Array[(String,String)] = {
    return paramSchema
  }
}

class WFConfigSchema {
  val schemaWF = new ConfigSchemaTuples(
    Array(
      ("site_id","string"),
      ("date","string"),
      ("lane","int"),
      ("lane_name","string"),
      ("direction","int"),
      ("direction_name","string"),
      ("reverse","int"),
      ("class_scheme","int"),
      ("class","int"),
      ("class_name","string"),
      ("length","float"),
      ("headway","float"),
      ("gap","float"),
      ("speed","float"),
      ("weight","float"),
      ("vehicle_id","string"),
      ("flags","int"),
      ("flag_text","string"),
      ("num_axles","int"),
      ("axle_weights","string"),
      ("axle_spacings","string")
    )
  )

  def getConfigSchema():  ConfigSchemaTuples ={
    return schemaWF
  }
}
