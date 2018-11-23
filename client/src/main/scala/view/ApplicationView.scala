package view

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import controller._

object ApplicationView {
  private var stage: Stage = new Stage()

  object viewSelector extends Enumeration {
    val LOGIN, TEAM, WAITING = Value
  }

  type ViewSelector = viewSelector.Value

  import viewSelector._

  def changeView(view: ViewSelector): Unit = view match {
    case LOGIN =>
      setupScene(title = ViewConfiguration.LoginTitle, form = ViewConfiguration.LoginForm, controller =
        Some(LoginController()))
    case TEAM =>
      setupScene(title = ViewConfiguration.TeamSelectionTitle, form = ViewConfiguration.TeamSelectionForm, controller
        = Some(TeamSelectionController()))
    case WAITING =>
      setupScene(title = ViewConfiguration.WaitingTitle, form = ViewConfiguration.WaitingForm, controller
        = None)
    case _ => hideView()
  }

  def setupStage(stage: Stage, height: Double, width: Double): Unit = {
    this.stage = stage
    stage setHeight height
    stage setWidth width
    stage setResizable false
    stage setOnCloseRequest (_ => {
      Platform exit()
      System exit 0
    })
  }

  def showView(): Unit =
    Platform runLater (() => {
      stage show()
    })

  def hideView(): Unit =
    Platform runLater (() => {
      stage hide()
    })

  private def setupScene(title: String, form: String, controller: Option[ViewController]) {
    val loader: FXMLLoader = new FXMLLoader(getClass getResource form)
    controller match {
      case Some(c) => loader setController c
      case _ => Unit
    }
    val scene: Scene = new Scene(loader.load())
    Platform runLater (() => {
      stage setTitle title
      stage setScene scene
    })
  }
}