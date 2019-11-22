package damlcommon

type CreateContract struct {
	TemplateID Template    `json:"templateId"`
	Argument   interface{} `json:"argument"`
}

type Template struct {
	ModuleName string `json:"moduleName"`
	EntityName string `json:"entityName"`
}
