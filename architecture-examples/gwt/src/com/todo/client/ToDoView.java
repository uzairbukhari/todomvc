package com.todo.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.todo.client.ToDoPresenter.ViewEventHandler;

/**
 * A view for the {@link ToDoPresenter}
 *
 */
public class ToDoView extends Composite implements ToDoPresenter.View {

	private static ToDoViewUiBinder uiBinder = GWT.create(ToDoViewUiBinder.class);

	interface ToDoViewUiBinder extends UiBinder<Widget, ToDoView> {
	}

	@UiField
	Hyperlink routingAll;

	@UiField
	Hyperlink routingActive;

	@UiField
	Hyperlink routingCompleted;

	@UiField
	TextBoxWithPlaceholder taskText;

	@UiField
	Element remainingTasksCount;

	@UiField
	SpanElement remainingTasksLabel;

	@UiField
	Element mainSection;

	@UiField
	Element todoStatsContainer;

	@UiField
	SpanElement clearTasksCount;

	@UiField
	Button clearCompleted;

	@UiField
	InputElement toggleAll;

	@UiField(provided = true)
	CellList<ToDoItem> todoTable = new CellList<ToDoItem>(new ToDoCell());

	public ToDoView() {
		initWidget(uiBinder.createAndBindUi(this));

		// removes the yellow highlight
		todoTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

		// add IDs to the elements that have ui:field attributes. This is required because the UiBinder
		// does not permit the addition of ID attributes to elements marked with ui:field.
		// *SIGH*
		mainSection.setId("main");
		clearCompleted.getElement().setId("clear-completed");
		taskText.getElement().setId("new-todo");
		todoStatsContainer.setId("footer");
		toggleAll.setId("toggle-all");
	}

	@Override
	public String getTaskText() {
		return taskText.getText();
	}

	@Override
	public void addhandler(final ViewEventHandler handler) {

		// wire-up the events from the UI to the presenter.

		// The TodoMVC project template has a markup / style that is not compatible with the markup
		// generated by the GWT CheckBox control. For this reason, here we are using an InputElement
		// directly. As a result, we handle low-level DOM events rather than the GWT higher level
		// abstractions, e.g. ClickHandlers. A typical GWT application would not do this, however,
		// this nicely illustrates how you can develop GWT applications
		// that program directly against the DOM.
		final com.google.gwt.user.client.Element clientToggleElement = toggleAll.cast();
		DOM.sinkEvents(clientToggleElement, Event.ONCLICK);
		DOM.setEventListener(clientToggleElement, new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				handler.markAllCompleted(toggleAll.isChecked());
			}
		});

		taskText.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					handler.addTask();
				}
			}
		});

		clearCompleted.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handler.clearCompletedTasks();
			}
		});
	}

	@Override
	public void setDataProvider(AbstractDataProvider<ToDoItem> data) {
		data.addDataDisplay(todoTable);
	}

	@Override
	public void clearTaskText() {
		taskText.setText("");
	}

	@Override
	public void setTaskStatistics(int totalTasks, int completedTasks) {
		int remainingTasks = totalTasks - completedTasks;

		hideElement(mainSection, totalTasks == 0);
		hideElement(todoStatsContainer, totalTasks == 0);
		hideElement(clearCompleted.getElement(), completedTasks == 0);

		remainingTasksCount.setInnerText(Integer.toString(remainingTasks));
		remainingTasksLabel.setInnerText(remainingTasks > 1 || remainingTasks == 0 ? "items" : "item");
		clearTasksCount.setInnerHTML(Integer.toString(completedTasks));

		toggleAll.setChecked(totalTasks == completedTasks);
	}

	@Override
	public void setRouting(ToDoRouting routing) {
		selectRoutingHyperlink(routingAll, ToDoRouting.ALL, routing);
		selectRoutingHyperlink(routingActive, ToDoRouting.ACTIVE, routing);
		selectRoutingHyperlink(routingCompleted, ToDoRouting.COMPLETED, routing);
	}

	private void selectRoutingHyperlink(Hyperlink hyperlink, ToDoRouting currentRoutingState,
	    ToDoRouting routingStateToMatch) {
		if (currentRoutingState == routingStateToMatch) {
			hyperlink.getElement().addClassName("selected");
		} else {
			hyperlink.getElement().removeClassName("selected");
		}
	}

	private void hideElement(Element element, boolean hide) {
		if (hide) {
			element.setAttribute("style", "display:none;");
		} else {
			element.setAttribute("style", "display:block;");
		}
	}
}
