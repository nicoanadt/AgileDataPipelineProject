package uk.ac.man.cs.agiledata.cfg

/**
 * An object that contains an array of tuples (string and string)
 * @param paramSchema
 */
class ConfigSchemaTuples(paramSchema: Array[(String,String)]) {
  def getConfigSchema(): Array[(String,String)] = {
    return paramSchema
  }
}


/**
 * This class is responsible to convert workflow configuration from the database to the
 * WFConfigSchema object that can be read by the AdalineRunApp
 *
 * @param configFromDB  WFConfig object obtained from the MongoDB metastore database
 */
class WFConfigSchema(configFromDB: WFConfig) {

  // Move schema from db macros to our array
  val schemaListFromDB =  configFromDB.workflow.source.schema
  val schemaArrFromDB = schemaListFromDB.map(x => (x.name,x.datatype)).toArray
  val schemaWF = new ConfigSchemaTuples(schemaArrFromDB)

  val schemaWF_BAK = new ConfigSchemaTuples(
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
