//
//  CloudNode.cpp
//  WeatherDefender
//
//  Created by Stefan Joseph on 3/18/19.
//  Copyright © 2019 Cornell Game Design Initiative. All rights reserved.
//

#include "CloudNode.hpp"
#include <algorithm>
#include <cugl/2d/CUPolygonNode.h>
#include <cugl/2d/CUScene.h>

using namespace cugl;
/**
 * Sets the texture polygon to the vertices expressed in image space.
 *
 * The polygon will be triangulated using the rules of SimpleTriangulator.
 * All PolygonNode objects share a single triangulator, so this method is
 * not thread safe.
 *
 * @param   vertices The vertices to texture
 * @param   offset   The offset in vertices
 * @param   size     The number of elements in vertices
 */
void CloudNode::setPolygon(const std::vector<Vec2>& vertices) {
    _polygon.set(vertices);
    _polygon.getIndices().clear();
    _triangulator.set(vertices);
    _triangulator.calculate();
    _triangulator.getTriangulation(_polygon.getIndices());
    TexturedNode::setPolygon(_polygon);
}

/**
 * Sets the polygon to the given one in texture space.
 *
 * @param poly  The polygon to texture
 */
void CloudNode::setPolygon(const Poly2& poly) {
    if (&_polygon != &poly) {
        CUAssertLog(poly.getType() == Poly2::Type::SOLID,
                    "The polygon is not solid");
        _polygon.set(poly);
    }
    
    setContentSize(_polygon.getBounds().size);
}

/**
 * Sets the texture polygon to one equivalent to the given rect.
 *
 * The rectangle will be converted into a Poly2, using the standard (solid)
 * triangulation.  This is the same as passing Poly2(rect,true). This will
 * not size the image to fit the rectangle.  Instead, it uses the rectangle
 * to define the portion of the image that will be displayed.
 *
 * @param rect  The rectangle to texture
 */
void CloudNode::setPolygon(const Rect& rect) {
    _polygon.set(rect);
    setContentSize(_polygon.getBounds().size);
}

/**
 * Draws this Node via the given SpriteBatch.
 *
 * This method only worries about drawing the current node.  It does not
 * attempt to render the children.
 *
 * This is the method that you should override to implement your custom
 * drawing code.  You are welcome to use any OpenGL commands that you wish.
 * You can even skip use of the SpriteBatch.  However, if you do so, you
 * must flush the SpriteBatch by calling end() at the start of the method.
 * in addition, you should remember to call begin() at the start of the
 * method.
 *
 * This method provides the correct transformation matrix and tint color.
 * You do not need to worry about whether the node uses relative color.
 * This method is called by render() and these values are guaranteed to be
 * correct.  In addition, this method does not need to check for visibility,
 * as it is guaranteed to only be called when the node is visible.
 *
 * @param batch     The SpriteBatch to draw with.
 * @param matrix    The global transformation matrix.
 * @param tint      The tint to blend with the Node color.
 */
void CloudNode::draw(const std::shared_ptr<SpriteBatch>& batch, const Mat4& transform, Color4 tint) {
    if (!_rendered) {
        generateRenderData();
    }

    batch->end();
    //my custom code
    CULogGLError();
    ps.drawParticles();
    CULogGLError();
    batch->begin(getScene()->getCamera()->getCombined());

    batch->setColor(tint);
    batch->setTexture(_texture);
    batch->setBlendEquation(_blendEquation);
    batch->setBlendFunc(_srcFactor, _dstFactor);
    batch->fill(_vertices.data(),(unsigned int)_vertices.size(),0,
                _polygon.getIndices().data(),(unsigned int)_polygon.getIndices().size(),0,transform);
}

/** A triangulator for those incomplete polygons */
SimpleTriangulator CloudNode::_triangulator;




