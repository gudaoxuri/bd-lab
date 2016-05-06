package run.mind.lab.visitanalysis

case class VisitDTO(
                     url: String,
                     ip: String,
                     cookies: String
                   ) extends Serializable {
}

object VisitDTO extends Serializable {
  val SPLIT = "\\|"

  def apply(str: String): VisitDTO = {
    val fields = str.split(SPLIT)
    if(fields.length==3){
      new VisitDTO(fields(0), fields(1), fields(2))
    }else{
      null
    }
  }

}
