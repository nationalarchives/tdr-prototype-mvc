package scala.steps

import java.util
import java.util.Locale

import io.cucumber.core.api.{TypeRegistry, TypeRegistryConfigurer}
import io.cucumber.datatable.{DataTableType, TableEntryTransformer}

class TableConfigurer extends TypeRegistryConfigurer{

  override def configureTypeRegistry(typeRegistry: TypeRegistry): Unit = {
    typeRegistry.defineDataTableType(new DataTableType(classOf[FieldInsertData], new TableEntryTransformer[FieldInsertData] {
      override def transform(entry: util.Map[String, String]): FieldInsertData = FieldInsertData(entry.get("selector"), entry.get("value"), entry.get("fieldType"))
    }))

    typeRegistry.defineDataTableType(new DataTableType(classOf[DatabaseResponseData], new TableEntryTransformer[DatabaseResponseData] {
      override def transform(entry: util.Map[String, String]): DatabaseResponseData = DatabaseResponseData(entry.get("field"), entry.get("value"))
    }))
  }

  override val locale = Locale.ENGLISH

}
