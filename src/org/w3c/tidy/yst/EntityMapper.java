package org.w3c.tidy.yst;

public class EntityMapper {

  private Configuration configuration;
  public EntityMapper(Configuration configuration) {
    this.configuration = configuration;
  }

  public String mapChar(int c) {
    String entity;
    boolean breakable = false; // #431953 - RJ
    String result;

    EntityTable eTable = EntityTable.getDefaultEntityTable();
//    // don't map latin-1 chars to entities
//    if (this.configuration.getOutCharEncoding() == Configuration.LATIN1) {
      if (c > 255 || c > 126 && c < 160) { /* multi byte chars */
        if (!this.configuration.numEntities) {
          entity = eTable.entityName( (short)c);
          if (entity != null) {
            entity = "&" + entity + ";";
          } else {
            entity = "&#" + c + ";";
          }
        } else {
          entity = "&#" + c + ";";
        }

        return entity;
      }

      switch ((short)c) {
        case 34 : return "&quot;";
        case 38 : return "&amp;";
        case 60 : return "&lt;";
        case 62 : return "&gt;";
        default : return ""+(char)c;
      }
  }
}
