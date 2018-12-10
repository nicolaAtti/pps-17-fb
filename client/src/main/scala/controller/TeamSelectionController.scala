package controller

import java.net.URL
import java.util.ResourceBundle
import javafx.event.ActionEvent
import javafx.fxml.{FXML, Initializable}
import view.ApplicationView
import view.ViewConfiguration.viewSelector._
import alice.tuprolog.SolveInfo
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

  /** Return the TeamSelectionController. */
  val controller: ViewController = this

  /** The username of the player. */
  var username: String = "guest"

  @FXML var idLabel: Label = _
  @FXML var characterDescription: TextArea = _
  @FXML var Jacob: StackPane = _
  @FXML var chosenCharacter0: StackPane = _
  @FXML var gridChosen: GridPane = _
  @FXML var gridToChoose: GridPane = _
  @FXML var joinCasualMatch: Button = _

  private var selectedCharacter: StackPane = new StackPane()
  private var chosenCharacter: StackPane = new StackPane()
  private var team: Map[String, StackPane] = Map()
  private val NotChosenSaturation: Double = 0
  private val NotChosenBrightness: Double = 0
  private val ChosenSaturation: Double = -1
  private val ChosenBrightness: Double = 0.7

  /** Pressure handler of the "Logout" button. */
  @FXML def handleLogout(event: ActionEvent) {
    ApplicationView changeView LOGIN
  }

  /** Pressure handler of the "Join Casual Queue" button. */
  @FXML def handleJoinCasualQueue(event: ActionEvent) {
    ApplicationView changeView WAITING_OPPONENT
    val teamName: Map[String, String] = team.map { case (position, characterPane) => position -> characterPane.getId }
    MatchmakingManager.joinCasualQueueRequest(username, teamName)
  }

  /** Pressure handler of the characters to choose from. */
  @FXML def handleCharacterToChoosePressed(mouseEvent: MouseEvent) {
    val characterPressed: StackPane = mouseEvent.getSource.asInstanceOf[StackPane]
    changeCharacterToChoose(characterPressed)
  }

  /** Pressure handler of the chosen characters. */
  @FXML def handleCharacterChosenPressed(mouseEvent: MouseEvent) {
    val characterPressed: StackPane = mouseEvent.getSource.asInstanceOf[StackPane]
    changeCharacterChosen(characterPressed)
  }

  override def initialize(location: URL, resources: ResourceBundle): Unit = {
    idLabel setText username
    deselectAllCharacters(gridChosen, gridToChoose)
    chosenCharacter = chosenCharacter0
    selectedCharacter = Jacob
    selectCharacter(chosenCharacter)
    changeCharacterToChoose(Jacob)
  }

  private def setDescription(characterName: String): Unit = {
    import utilities.ScalaProlog._
    val solveInfo: SolveInfo = getCharacter(characterName)
    var description: String = "Character name: " + characterName + "\n" +
      "Character class: " + extractString(solveInfo, "Class") + "\n\n" +
      "Strength: " + extractInt(solveInfo, "Strength") + "\n" +
      "Agility: " + extractInt(solveInfo, "Agility") + "\n" +
      "Spirit: " + extractInt(solveInfo, "Spirit") + "\n" +
      "Intelligence: " + extractInt(solveInfo, "Intelligence") + "\n" +
      "Resistance: " + extractInt(solveInfo, "Resistance") + "\n\n" +
      "MoveList: \n"
    extractList(solveInfo, "MoveList").foreach(move =>
      extractString(getMove(move), "DamageType") match {
        case "PhysicalAttack" => description += "    " + move + "\n"
        case "StandardDamage" =>
          description += "    " + move +
            "     -> Type: " + extractString(getMove(move), "Type") +
            ", Base damage: " + extractInt(getMove(move), "BaseValue") +
            ", Mana cost: " + extractInt(getMove(move), "MPCost") +
            "\n"
        case "StandardHeal" =>
          description += "    " + move +
            "     -> Type: " + extractString(getMove(move), "Type") +
            ", Base heal: " + extractInt(getMove(move), "BaseValue") +
            ", Mana cost: " + extractInt(getMove(move), "MPCost") +
            "\n"
        case "Percentage" =>
          description += "    " + move +
            "     -> Type: " + extractString(getMove(move), "Type") +
            ", Percentage value: " + extractInt(getMove(move), "BaseValue") +
            ", Mana cost: " + extractInt(getMove(move), "MPCost") +
            "\n"
        case "BuffDebuff" =>
          description += "    " + move +
            "     -> Type: " + extractString(getMove(move), "Type") +
            ", Mana cost: " + extractInt(getMove(move), "MPCost") +
            "\n"

    })
    characterDescription.setText(description)
  }

  private def deselectAllCharacters(gridPanes: GridPane*): Unit = {
    gridPanes.foreach(gridPane =>
      gridPane.getChildren.forEach(pane => {
        deselectCharacter(pane.asInstanceOf[StackPane])
      }))
  }

  private def changeCharacterToChoose(next: StackPane): Unit = {
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

  private def changeCharacterChosen(next: StackPane): Unit = {
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

  private def changeSelectedCharacter(previous: StackPane, next: StackPane): Unit = {
    deselectCharacter(previous)
    selectCharacter(next)
  }

  private def selectCharacter(stackPane: StackPane): Unit = {
    stackPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("BLUE"), CornerRadii.EMPTY, Insets.EMPTY)))
  }

  private def deselectCharacter(stackPane: StackPane): Unit = {
    stackPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("WHITE"), CornerRadii.EMPTY, Insets.EMPTY)))
  }
}
