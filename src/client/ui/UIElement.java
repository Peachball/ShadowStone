package client.ui;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import utils.DefaultInputListener;
import utils.DefaultKeyListener;
import utils.DefaultMouseListener;

public class UIElement implements DefaultInputListener, UIEventListener {
	public static final double EPSILON = 0.0001;

	protected UI ui;
	UIElement parent = null;
	ArrayList<UIElement> children = new ArrayList<UIElement>();
	ArrayList<UIElement> childrenAddBuffer = new ArrayList<UIElement>();
	ArrayList<UIElement> childrenRemoveBuffer = new ArrayList<UIElement>();
	public int alignh = 0, alignv = 0;
	private boolean hide = false;
	public boolean draggable = false, ignorehitbox = false, hitcircle = false, clip = false, scrollable = false,
			hasFocus = false;
	public Vector2f childoffset = new Vector2f(), margins = new Vector2f(); // public
																			// cuz
																			// fuck
																			// u
	private Vector2f targetpos = new Vector2f(), pos = new Vector2f();
	public double scale = 1, speed = 1, angle = 0;
	Animation animation;
	Image finalImage;

	public UIElement(UI ui, Vector2f pos) {
		this.ui = ui;
		this.pos = pos.copy();
		this.targetpos = pos.copy();
	}

	public UIElement(UI ui, Vector2f pos, String imagepath) {
		this(ui, pos);
		this.setImage(imagepath);
	}

	public UIElement(UI ui, Vector2f pos, Animation animation) {
		this(ui, pos);
		this.setAnimation(animation);
	}

	public void setImage(String imagepath) {
		this.setAnimation(imagepath, new Vector2f(1, 1), 0, 0);
	}

	public void setAnimation(Animation animation) {
		this.animation = animation;
		this.finalImage = animation.getCurrentFrame().getScaledCopy((float) this.scale);
	}

	public void setAnimation(String imagepath, Vector2f framedim, int spacing, int margin) {
		if (imagepath != null && !imagepath.isEmpty()) {
			this.animation = new Animation(imagepath, framedim, spacing, margin);
			this.finalImage = this.animation.getCurrentFrame().getScaledCopy((float) this.scale);
		}
	}

	public Animation getAnimation() {
		return this.animation;
	}

	public void setPos(Vector2f pos, double speed) {
		this.targetpos.set(pos);
		this.speed = speed;
		if (speed == 1) {
			this.pos.set(pos);
		}
	}

	public Vector2f getPos() {
		return this.pos.copy();
	}

	public Vector2f getFinalPos() {
		if (this.parent != null) {
			return this.parent.getFinalPos().copy().add(this.parent.childoffset).add(this.pos);
		}
		return this.getPos();
	}

	public Vector2f getDim(boolean margin) {
		return new Vector2f((float) this.getWidth(margin), (float) this.getHeight(margin));
	}

	public double getWidth(boolean margin) {
		return this.finalImage.getWidth() - (margin ? this.margins.x * 2 : 0);
	}

	public double getHeight(boolean margin) {
		return this.finalImage.getHeight() - (margin ? this.margins.y * 2 : 0);
	}

	// defined as distance from pos to left edge
	public double getHOff() {
		return this.getWidth(false) * (this.alignh + 1) / 2;
	}

	// defined as distance from pos to top edge
	public double getVOff() {
		return this.getHeight(false) * (this.alignv + 1) / 2;
	}

	public double getLeft(boolean abs, boolean margin) {
		return (abs ? this.getFinalPos().x : this.getPos().x) - this.getHOff() + (margin ? this.margins.x : 0);
	}

	public double getRight(boolean abs, boolean margin) {
		return (abs ? this.getFinalPos().x : this.getPos().x) - this.getHOff() + this.getWidth(false)
				- (margin ? this.margins.x : 0);
	}

	public double getTop(boolean abs, boolean margin) {
		return (abs ? this.getFinalPos().y : this.getPos().y) - this.getVOff() + (margin ? this.margins.y : 0);
	}

	public double getBottom(boolean abs, boolean margin) {
		return (abs ? this.getFinalPos().y : this.getPos().y) - this.getVOff() + this.getHeight(false)
				- (margin ? this.margins.y : 0);
	}

	public double getLocalLeft(boolean margin) {
		return -this.getHOff() + (margin ? this.margins.x : 0);
	}

	public double getLocalRight(boolean margin) {
		return -this.getHOff() + this.getWidth(false) - (margin ? this.margins.x : 0);
	}

	public double getLocalTop(boolean margin) {
		return -this.getVOff() + (margin ? this.margins.y : 0);
	}

	public double getLocalBottom(boolean margin) {
		return -this.getVOff() + this.getHeight(false) - (margin ? this.margins.y : 0);
	}

	// topmost point relative to parent before childoffset
	public double getChildLocalTop(double offset) {
		double y = this.getLocalTop(false) + offset;
		for (UIElement u : this.getChildren()) {
			y = Math.min(y, u.getChildLocalTop(offset + u.pos.y));
		}
		return y;
	}

	public double getChildLocalBottom(double offset) {
		double y = this.getLocalBottom(false) + offset;
		for (UIElement u : this.getChildren()) {
			y = Math.max(y, u.getChildLocalBottom(offset + u.pos.y));

		}
		return y;
	}

	public void setHide(boolean hide) {
		this.hide = hide;
		if (hide && this.hasFocus) {
			this.ui.focusElement(this.parent);
		}
	}

	public boolean getHide() {
		return this.hide;
	}

	// do not override this shit
	public void alert(String strarg, int... intarg) {
		if (this.parent != null) {
			this.parent.onAlert(strarg, intarg);
		} else {
			this.ui.alertListeners(strarg, intarg);
		}
	}

	// override this shit tho
	public void onAlert(String strarg, int... intarg) {
		this.alert(strarg, intarg);
	}

	public boolean pointIsInHitbox(Vector2f pos) {
		if (this.hitcircle) {
			return (new Vector2f(
					(float) ((pos.x - this.getFinalPos().x + this.getWidth(false) / 2 - this.getHOff())
							/ this.getWidth(false)),
					(float) ((pos.y - this.getFinalPos().y + this.getHeight(false) / 2 - this.getVOff())
							/ this.getHeight(false))).length()) < 0.25;
		}
		return pos.getX() >= this.getLeft(true, false) && pos.getX() <= this.getRight(true, false)
				&& pos.getY() >= this.getTop(true, false) && pos.getY() <= this.getBottom(true, false);
	}

	public void fitInParent() {
		double x = this.pos.getX(), y = this.pos.getY();
		if (this.parent == null) { // is parent
			x = Math.max(x, this.getHOff());
			x = Math.min(x, Game.WINDOW_WIDTH - this.getHOff() + this.getWidth(false));
			y = Math.max(y, this.getVOff());
			y = Math.min(y, Game.WINDOW_HEIGHT - this.getVOff() + this.getHeight(false));
		} else { // has parent
			x = Math.max(x, -this.parent.getLocalLeft(true) + this.getHOff());
			x = Math.min(x, this.parent.getLocalRight(true) - this.getHOff() + this.getWidth(false));
			y = Math.max(y, -this.parent.getLocalTop(true) + this.getVOff());
			y = Math.min(y, this.parent.getLocalBottom(true) - this.getVOff() + this.getHeight(false));
		}
		this.setPos(new Vector2f((float) x, (float) y), 1);
	}

	public void update(double frametime) {
		if (this.animation != null) {
			this.animation.update(frametime);
		}
		Vector2f delta = this.targetpos.copy().sub(this.pos);
		if (delta.length() > EPSILON) {
			float ratio = 1 - (float) Math.pow(1 - this.speed, frametime);
			this.pos.add(delta.scale(ratio));
		}
		this.updateRelationships();
		for (UIElement u : this.getChildren()) {
			u.update(frametime);
		}
	}

	public void debugPrint(int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("|");
		}
		System.out.println(this.debug());
		for (UIElement u : this.getChildren()) {
			u.debugPrint(depth + 1);
		}
	}

	public String debug() {
		return this.getClass().toString().substring("class client.ui.".length()) + " "
				+ (this.getAnimation() != null ? this.getAnimation().getCurrentFrame() : "");
	}

	public void draw(Graphics g) {
		if (this.animation != null) {
			this.finalImage = this.animation.getCurrentFrame().getScaledCopy((float) this.scale);
			this.finalImage.rotate((float) this.angle);
			if (!this.hide) {
				g.drawImage(this.finalImage, (float) (this.getLeft(true, false)), (float) (this.getTop(true, false)));
			}
		}
		if (!this.hide) {
			this.drawChildren(g);
		}
	}

	public void drawChildren(Graphics g) {
		Rectangle prevClip = g.getClip();
		if (this.clip) {
			g.setClip((int) (this.getLeft(true, true)), (int) (this.getTop(true, true)), (int) this.getWidth(true),
					(int) this.getHeight(true));
		}
		for (UIElement u : this.getChildren()) {
			u.draw(g);

		}
		g.setClip(prevClip);
	}

	// returns the uielement that is top (prioritizes children)
	public UIElement topChildAtPos(Vector2f pos, boolean requirehitbox, boolean requirescrollable,
			boolean requiredraggable) {
		if (this.hide) {
			return null;
		}
		UIElement u = null;

		if ((!this.ignorehitbox || !requirehitbox) && (this.scrollable || !requirescrollable)
				&& (this.draggable || !requiredraggable)) {
			if (this.pointIsInHitbox(pos)) {
				u = this;
			} else if (this.clip) { // not in hitbox and we are clipping
				return u; // sucks to suck
			}
		}
		for (UIElement child : this.children) {
			UIElement thing = child.topChildAtPos(pos, requirehitbox, requirescrollable, requiredraggable);
			if (thing != null) {
				u = thing;
			}
		}
		return u;
	}

	// PARENTING //////////////////////////
	// massive copy paste fiesta down below
	public ArrayList<UIElement> getChildren() {
		ArrayList<UIElement> allchildren = new ArrayList<UIElement>();
		allchildren.addAll(this.children);
		allchildren.addAll(this.childrenAddBuffer);
		allchildren.removeAll(this.childrenRemoveBuffer);
		return allchildren;

	}

	public void setParent(UIElement parent) { // should only be run once per
												// thing
		this.parent = parent;
		if (!parent.children.contains(this) && !parent.childrenAddBuffer.contains(this)) {
			parent.childrenAddBuffer.add(this);
		}
	}

	public UIElement getParent() {
		return this.parent;
	}

	public void addChild(UIElement child) {
		child.setParent(this);
	}

	/**
	 * Removes the parent of the UIElement, handles the removing of the
	 * connection between parent and child by removing itself from the parent's
	 * children and by removing its parent object
	 */
	public void removeParent() {
		if (this.parent != null) {
			// this.setPos(this.parent.getFinalPos().add(this.pos), 1);
			this.parent.childrenRemoveBuffer.add(this);
		}
		this.parent = null;
	}

	public void updateRelationships() {
		this.children.addAll(this.childrenAddBuffer);
		this.children.removeAll(this.childrenRemoveBuffer);
		this.childrenAddBuffer.clear();
		this.childrenRemoveBuffer.clear();
	}

	/**
	 * Removes a child of an UIElement
	 * 
	 * @param child
	 *            The child to remove
	 */
	public void removeChild(UIElement child) {
		if (this.getChildren().contains(child)) {
			child.removeParent();
		}
	}

	public void removeChildren() {
		for (UIElement u : this.getChildren()) {
			u.removeParent();
		}
	}

}
