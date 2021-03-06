package controller

import java.net.URL
import java.util.ResourceBundle
import javafx.event.ActionEvent
import javafx.fxml.{FXML, Initializable}
import view.ApplicationView
import view.ViewConfiguration.ViewSelector._
import javafx.geometry.Insets
import javafx.scene.control.{Button, Label, TextArea}
import javafx.scene.effect.ColorAdjust
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout._
import javafx.scene.paint.Paint
import messaging.MatchmakingManager

/** Controller of the team selection view.
  *
  * @author Daniele Schiavi
  */
object TeamSelectionController extends Initializable with ViewController {

  import SelectionHelpers._

  val controller: ViewController = this
  var username: String = "guest"
  var team: Map[String, StackPane] = Map()

  /** Visual properties of not chosen characters. */
  val NotChosenSaturation: Double = 0
  val NotChosenBrightness: Double = 0

  /** Visual properties of chosen characters. */
  val ChosenSaturation: Double = -1
  val ChosenBrightness: Double = 0.7

  @FXML var idLabel: Label = _
  @FXML var characterDescription: TextArea = _
  @FXML var Jacob: StackPane = _
  @FXML var chosenCharacter0: StackPane = _
  @FXML var gridChosen: GridPane = _
  @FXML var gridToChoose: GridPane = _
  @FXML var joinCasualMatch: Button = _

  /** Handler of the "Logout" button action. */
  @FXML def handleLogout(event: ActionEvent) {
    ApplicationView changeView LOGIN
  }

  /** Handler of the "Join Casual Queue" button action. */
  @FXML def handleJoinCasualQueue(event: ActionEvent) {
    ApplicationView changeView WAITING_OPPONENT
    val teamName: Set[String] = team.map { case (_, characterPane) => characterPane.getId }.toSet
    MatchmakingManager.joinCasualQueueRequest(username, teamName)
  }

  /** Handler of the "Moves manual" button action. */
  @FXML def movesManualPressed(event: ActionEvent) {
    ApplicationView.createMovesManualView()
  }

  /** Handles the selection of an available character to choose. */
  @FXML def handleCharacterToChoosePressed(mouseEvent: MouseEvent) {
    changeCharacterToChoose(mouseEvent.getSource.asInstanceOf[StackPane])
  }

  /** Handles the selection of a chosen character. */
  @FXML def handleCharacterChosenPressed(mouseEvent: MouseEvent) {
    changeCharacterChosen(mouseEvent.getSource.asInstanceOf[StackPane])
  }

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    idLabel setText username
    deselectAllCharacters(gridChosen, gridToChoose)
    chosenCharacter = chosenCharacter0
    selectedCharacter = Jacob
    selectCharacter(chosenCharacter)
    changeCharacterToChoose(Jacob)
  }

  private object SelectionHelpers {
    var selectedCharacter: StackPane = new StackPane()
    var chosenCharacter: StackPane = new StackPane()
    val SelectedColor: Paint = Paint.valueOf("BLUE")
    val DeselectedColor: Paint = Paint.valueOf("WHITE")

    def setDescription(characterName: String): Unit = {
      import utilities.ScalaProlog._
      val character = getCharacter(characterName)
      var description: String = "Character name: " + characterName + "\n" +
        "Character class: " + character.role + ", HP: " + character.status.maxHealthPoints + " MP: " + character.status.maxManaPoints + "\n\n" +
        "Strength: " + character.statistics.strength + "\n" +
        "Agility: " + character.statistics.agility + "\n" +
        "Spirit: " + character.statistics.spirit + "\n" +
        "Intelligence: " + character.statistics.intelligence + "\n" +
        "Resistance: " + character.statistics.resistance + "\n\n" +
        "Special moves: \n\n"
      character.specialMoves.foreach {
        case (_, move) =>
          description += s"    [${move.name}]  Type: ${move.moveType} | Mana cost: ${move.manaCost} | Max targets: ${move.maxTargets}\n"
      }
      characterDescription.setText(description)
    }

    def deselectAllCharacters(gridPanes: GridPane*): Unit = {
      gridPanes.foreach(gridPane =>
        gridPane.getChildren.forEach(pane => {
          deselectCharacter(pane.asInstanceOf[StackPane])
        }))
    }

    /** Changes the currently selected character (from all the available characters),
      * given a new stack pane.
      *
      * @param next the newly selected character's stack pane
      */
    def changeCharacterToChoose(next: StackPane): Unit = {
      changeSelectedCharacter(selectedCharacter, next)
      selectedCharacter = next
      setDescription(next.getId)
      if (!team.exists { case (_, characterPane) => characterPane equals next }) {
        chosenCharacter.getChildren
          .get(0)
          .asInstanceOf[ImageView]
          .setImage(selectedCharacter.getChildren.get(0).asInstanceOf[ImageView].getImage)
        team += (chosenCharacter.getId -> next)
        if (team.size == 4)
          joinCasualMatch.setDisable(false)
      }
    }

    /** Changes the currently selected character (from the player's team), given a
      * new stack pane.
      *
      * @param next the newly selected character's stack pane
      */
    def changeCharacterChosen(next: StackPane): Unit = {
      changeSelectedCharacter(chosenCharacter, next)
      chosenCharacter = next
      team.foreach {
        case (position, characterPane) =>
          val effect: ColorAdjust = new ColorAdjust()
          if (position equals chosenCharacter.getId) {
            effect setSaturation NotChosenSaturation
            effect setBrightness NotChosenBrightness
            changeCharacterToChoose(characterPane)
          } else {
            effect setSaturation ChosenSaturation
            effect setBrightness ChosenBrightness
          }
          characterPane.getChildren.get(0).asInstanceOf[ImageView] setEffect effect
      }
    }

    def changeSelectedCharacter(previous: StackPane, next: StackPane): Unit = {
      deselectCharacter(previous)
      selectCharacter(next)
    }

    def selectCharacter(stackPane: StackPane): Unit = {
      stackPane.setBackground(new Background(new BackgroundFill(SelectedColor, CornerRadii.EMPTY, Insets.EMPTY)))
    }

    def deselectCharacter(stackPane: StackPane): Unit = {
      stackPane.setBackground(new Background(new BackgroundFill(DeselectedColor, CornerRadii.EMPTY, Insets.EMPTY)))
    }
  }
}
