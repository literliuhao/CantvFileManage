package com.cantv.media.center.data;

import java.util.List;

public class MenuItem {

	public static final int TYPE_NORMAL = 1;
	public static final int TYPE_LIST = 2;
	public static final int TYPE_SELECTOR = 3;

	protected String title;
	protected int type = TYPE_NORMAL;
	protected boolean isSelected;
	protected boolean enabled = true;

	protected MenuItem parent;
	protected List<MenuItem> children;
	protected MenuItem selectedChild;

	public MenuItem() {
		super();
	}

	public MenuItem(String title) {
		super();
		this.title = title;
	}

	public MenuItem(String title, int type) {
		super();
		this.title = title;
		this.type = type;
	}

	public MenuItem(String title, MenuItem parent) {
		super();
		this.title = title;
		this.parent = parent;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		if (parent != null) {
			parent.setSelectedChild(this);
		}
	}

	public MenuItem getParent() {
		return parent;
	}

	public void setParent(MenuItem parent) {
		this.parent = parent;
	}

	public List<MenuItem> getChildren() {
		return children;
	}

	public int getChildrenCount() {
		return children == null ? 0 : children.size();
	}

	public void setChildren(List<MenuItem> children) {
		if (children != null) {
			for (MenuItem item : children) {
				item.setParent(this);
			}
		}
		this.children = children;
	}

	public MenuItem getSelectedChild() {
		return selectedChild;
	}
	
	public int getSelectedChildIndex() {
		if(children != null && selectedChild != null){
			return children.indexOf(selectedChild);
		}
		return 0;
	}

	public MenuItem getChildAt(int index) {
		int childrenCount = getChildrenCount();
		if (childrenCount == 0 || index < 0 || index >= childrenCount) {
			return null;
		}
		return children.get(index);
	}

	public void setSelectedChild(MenuItem selectedChild) {
		this.selectedChild = selectedChild;
	}

	public int setChildSelected(int index) {
		int childrenCount = getChildrenCount();
		if (childrenCount == 0 || index < 0 || index >= childrenCount) {
			return 0;
		}
		int oldIndex = 0;
		if (selectedChild != null) {
			selectedChild.setSelected(false);
			oldIndex = children.indexOf(selectedChild);
		}
		selectedChild = getChildren().get(index);
		selectedChild.setSelected(true);
		return oldIndex;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "MenuItem [title=" + title + ", type=" + type + ", isSelected=" + isSelected + ", parent=" + parent
				+ ", children=" + children + ", selectedChild=" + selectedChild + "]";
	}

}
