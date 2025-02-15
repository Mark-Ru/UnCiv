package com.unciv.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.unciv.UnCivGame
import com.unciv.models.gamebasics.GameBasics
import com.unciv.models.gamebasics.tr
import com.unciv.ui.utils.*
import java.util.*

class CivilopediaScreen : CameraStageBaseScreen() {
    class CivilopediaEntry {
        var name: String
        var description: String
        var image: Actor?=null

        constructor(name: String, description: String, image: Actor?=null) {
            this.name = name
            this.description = description
            this.image = image
        }

        constructor() : this("","") // Needed for GameBAsics json deserializing
    }

    val categoryToEntries = LinkedHashMap<String, Collection<CivilopediaEntry>>()
    val categoryToButtons = LinkedHashMap<String, Button>()

    val entrySelectTable = Table().apply { defaults().pad(5f) }
    val description = "".toLabel()

    fun select(category: String) {
        entrySelectTable.clear()
        for (entry in categoryToEntries[category]!!
                .sortedBy { it.name.tr() }){  // Alphabetical order of localized names
            val entryButton = Button(skin)
            if(entry.image!=null)
                entryButton.add(entry.image).size(50f).padRight(10f)
            entryButton.add(entry.name.toLabel())
            entryButton.onClick {
                description.setText(entry.description)
            }
            entrySelectTable.add(entryButton).row()
        }
    }

    init {
        onBackButtonClicked { UnCivGame.Current.setWorldScreen() }
        val buttonTable = Table()
        buttonTable.pad(15f)
        val entryTable = Table()
        val splitPane = SplitPane(buttonTable, entryTable, true, skin)
        splitPane.splitAmount = 0.2f
        splitPane.setFillParent(true)

        stage.addActor(splitPane)

        description.setWrap(true)

        val goToGameButton = TextButton("Close".tr(), skin)
        goToGameButton.onClick {
                game.setWorldScreen()
                dispose()
            }
        buttonTable.add(goToGameButton)



        val language = UnCivGame.Current.settings.language.replace(" ","_")
        val basicHelpFileName = if(Gdx.files.internal("jsons/BasicHelp/BasicHelp_$language.json").exists())"BasicHelp/BasicHelp_$language"
        else "BasicHelp/BasicHelp"


        categoryToEntries["Basics"] = GameBasics.getFromJson(kotlin.Array<CivilopediaEntry>::class.java, basicHelpFileName).toList()
        categoryToEntries["Buildings"] = GameBasics.Buildings.values
                .map { CivilopediaEntry(it.name,it.getDescription(false, null),
                        ImageGetter.getConstructionImage(it.name)) }
        categoryToEntries["Resources"] = GameBasics.TileResources.values
                .map { CivilopediaEntry(it.name,it.getDescription(),
                        ImageGetter.getResourceImage(it.name,50f)) }
        categoryToEntries["Terrains"] = GameBasics.Terrains.values
                .map { CivilopediaEntry(it.name,it.getDescription()) }
        categoryToEntries["Tile Improvements"] = GameBasics.TileImprovements.values
                .map { CivilopediaEntry(it.name,it.getDescription(),
                        ImageGetter.getImprovementIcon(it.name,50f)) }
        categoryToEntries["Units"] = GameBasics.Units.values
                .map { CivilopediaEntry(it.name,it.getDescription(false),
                        ImageGetter.getConstructionImage(it.name)) }
        categoryToEntries["Technologies"] = GameBasics.Technologies.values
                .map { CivilopediaEntry(it.name,it.getDescription(),
                        ImageGetter.getTechIconGroup(it.name,50f)) }

        categoryToEntries["Tutorials"] = Tutorials().getTutorialsOfLanguage("English").keys
                .filter { !it.startsWith("_") }
                .map { CivilopediaEntry(it.replace("_"," "),
                        Tutorials().getTutorials(it, UnCivGame.Current.settings.language)
                                .joinToString("\n\n")) }

        for (category in categoryToEntries.keys) {
            val button = TextButton(category.tr(), skin)
            button.style = TextButton.TextButtonStyle(button.style)
            categoryToButtons[category] = button
            button.onClick { select(category) }
            buttonTable.add(button)
        }
        select("Basics")
        val sp = ScrollPane(entrySelectTable)
        sp.setupOverscroll(5f, 1f, 200f)
        entryTable.add(sp).width(Value.percentWidth(0.25f, entryTable)).height(Value.percentHeight(0.7f, entryTable))
                .pad(Value.percentWidth(0.02f, entryTable))
        entryTable.add(description).colspan(4).width(Value.percentWidth(0.65f, entryTable)).height(Value.percentHeight(0.7f, entryTable))
                .pad(Value.percentWidth(0.02f, entryTable))
        // Simply changing these to x*width, y*height won't work

        buttonTable.width = stage.width
    }

}

