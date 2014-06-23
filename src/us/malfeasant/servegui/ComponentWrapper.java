package us.malfeasant.servegui;

class ComponentWrapper {
	final ComponentType type;
	final Object component;
	
	ComponentWrapper(ComponentType type, Object comp) {
		this.type = type;
		component = comp;
	}
}
