package com.unciv.ui.pickerscreens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup
import com.badlogic.gdx.utils.Align
import com.unciv.UnCivGame
import com.unciv.logic.map.MapUnit
import com.unciv.models.gamebasics.GameBasics
import com.unciv.models.gamebasics.Translations
import com.unciv.models.gamebasics.tr
import com.unciv.models.gamebasics.unit.Promotion
import com.unciv.ui.utils.*

class PromotionPickerScreen(val mapUnit: MapUnit) : PickerScreen() {
    private var selectedPromotion: Promotion? = null


    fun acceptPromotion(promotion: Promotion?) {
        mapUnit.promotions.addPromotion(promotion!!.name)
        if(mapUnit.promotions.canBePromoted()) game.setScreen(PromotionPickerScreen(mapUnit))
        else game.setWorldScreen()
        dispose()
        game.worldScreen.shouldUpdate=true
    }

    init {
        onBackButtonClicked { UnCivGame.Current.setWorldScreen() }
        setDefaultCloseAction()


        rightSideButton.setText("Pick promotion".tr())
        rightSideButton.onClick("promote") {
          acceptPromotion(selectedPromotion)
        }

        val availablePromotionsGroup = VerticalGroup()
        availablePromotionsGroup.space(10f)

        val unitType = mapUnit.type
        val promotionsForUnitType = GameBasics.UnitPromotions.values.filter { it.unitTypes.contains(unitType.toString()) }
        val unitAvailablePromotions = mapUnit.promotions.getAvailablePromotions()

        for (promotion in promotionsForUnitType) {
            if(promotion.name=="Heal Instantly" && mapUnit.health==100) continue
            val isPromotionAvailable = promotion in unitAvailablePromotions
            val unitHasPromotion = mapUnit.promotions.promotions.contains(promotion.name)

            val selectPromotionButton = Button(skin)
            selectPromotionButton.add(ImageGetter.getPromotionIcon(promotion.name)).size(30f).pad(10f)
            selectPromotionButton.add(promotion.name.toLabel()).pad(10f).padRight(20f)
            selectPromotionButton.touchable = Touchable.enabled
            selectPromotionButton.onClick {
                selectedPromotion = promotion
                rightSideButton.setText(promotion.name.tr())
                if(isPromotionAvailable && !unitHasPromotion) rightSideButton.enable()
                else rightSideButton.disable()

                // we translate it before it goes in to get uniques like "vs units in rough terrain" and after to get "vs city
                var descriptionText = Translations.translateBonusOrPenalty(promotion.effect.tr())

                if(promotion.prerequisites.isNotEmpty()) {
                    val prerequisitesString:ArrayList<String> = arrayListOf()
                    for (i in promotion.prerequisites.filter { promotionsForUnitType.any { promotion ->  promotion.name==it } }){
                        prerequisitesString.add(i.tr())
                    }
                    descriptionText +="\n{Requires}: ".tr()+prerequisitesString.joinToString(" OR ".tr())
                }
                descriptionLabel.setText(descriptionText)
            }

            val promotionTable = Table()
            promotionTable.add(selectPromotionButton)


            if(isPromotionAvailable) {
                val pickNow = "Pick now!".toLabel()
                pickNow.setAlignment(Align.center)
                pickNow.onClick {
                    acceptPromotion(promotion)
                }
                promotionTable.add(pickNow).padLeft(10f).fillY()
            }
            else if(unitHasPromotion) selectPromotionButton.color= Color.GREEN
            else selectPromotionButton.color= Color.GRAY

            availablePromotionsGroup.addActor(promotionTable)
        }
        topTable.add(availablePromotionsGroup)
    }
}