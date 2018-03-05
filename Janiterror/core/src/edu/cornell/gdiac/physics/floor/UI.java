package edu.cornell.gdiac.physics.floor;

import com.badlogic.gdx.graphics.Color;
import edu.cornell.gdiac.physics.GameCanvas;

public class UI {
    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */

    //depends on JoeModel to get hp
    //depends on JoeModel to get weapon durability

    public void draw(GameCanvas canvas) {
//        canvas.draw(texture, Color.WHITE, 0, 0);

        //Life Counter

        //n_lives = Get HP field of JoeModel
        //margin_counter = 0
        //for life in n_lives
            //margin_counter += 15 (or whatever spacing between pictures)
            //canvas.drawLife(x, margin_counter)

        //Weapon Icons

        //weapon_one = JoeModel.get_weapon1()
        //weapon_two = JoeModel.get_weapon2()

        //if weapon_one != False:
            //weapon_one = weapon_map[weapon_one]
                //return image to draw from weapon_map
            //draw image
        //else if weapon_two != False:
            //weapon_two = weapon_map[weapon_two]
                //return image to draw from weapon_map
            //draw image

        //Durability Counter

        //durability_one = JoeModel.get_durability_one()
        //durability_two = JoeModel.get_durability_two()
        //box2d.ProgressBar(float min, float max, float stepSize, boolean vertical, ProgressBar.ProgressBarStyle style)
    }
}
