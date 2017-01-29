package com.badlogic.gdx.physics.bullet;

import java.util.Arrays;

import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
//import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
//import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
//import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.linearmath.LinearMathConstants;
import com.badlogic.gdx.physics.bullet.linearmath.btScalar;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.SharedLibraryLoader;
/*[0;X;D]*/

/** @author xpenatan */
public class Bullet {

	/** The version of the Bullet library used by this wrapper. */
	public final static int VERSION = LinearMathConstants.BT_BULLET_VERSION;

	protected static boolean enableLogging = true;

	private static boolean bulletInit = false;

	/** Loads the native Bullet native library and initializes the gdx-bullet extension. Must be called before any of the bullet
	 * classes/methods can be used. */
	public static void init () {
		init(true);
	}

	/** Loads the native Bullet native library and initializes the gdx-bullet extension. Must be called before any of the bullet
	 * classes/methods can be used.
	 * @param useRefCounting Whether to use reference counting, causing object to be destroyed when no longer referenced. You must
	 *           use {@link BulletBase#obtain()} and {@link BulletBase#release()} when using reference counting.
	 * @param logging Whether to log an error on potential errors in the application. */
	public static void init (boolean logging) {
		if(Bullet.bulletInit)
			return;
		Bullet.bulletInit = true;
		Bullet.enableLogging = logging;
		new SharedLibraryLoader().load("gdx-bullet");
		final int version = btScalar.btGetVersion();
		if (version != VERSION)
			throw new GdxRuntimeException("Bullet binaries version (" + version + ") does not match source version (" + VERSION
				+ ")");
	}
	/*[0;X]
		if(Bullet.bulletInit)
			return;
		Bullet.bulletInit = true;
		Bullet.enableLogging = logging;
		final int version = btScalar.btGetVersion();
		if (version != VERSION)
			throw new GdxRuntimeException("Bullet binaries version (" + version + ") does not match source version (" + VERSION
				+ ")");
	*/

	/**
	 * Dispose static temporary objects. Use when ending app.
	 */
	public static void dispose() {

	}
	/*[0;X]
		com.badlogic.gdx.physics.bullet.linearmath.btVector3.btVector3_1.dispose();
		com.badlogic.gdx.physics.bullet.linearmath.btVector3.btVector3_2.dispose();
	*/


	protected static class ShapePart {
		public Array<MeshPart> parts = new Array<>();
		public Matrix4 transform = new Matrix4();
	}

	private final static Pool<ShapePart> shapePartPool = new Pool<ShapePart>() {
		@Override
		protected ShapePart newObject () {
			return new ShapePart();
		}
	};
	private final static Array<ShapePart> shapePartArray = new Array<>();

	private final static Matrix4 idt = new Matrix4();
	private final static Matrix4 tmpM = new Matrix4();

	public static void getShapeParts (final Node node, final boolean applyTransform, final Array<ShapePart> out, final int offset,
		final Pool<ShapePart> pool) {
		final Matrix4 transform = applyTransform ? node.localTransform : idt;
		if (node.parts.size > 0) {
			ShapePart part = null;
			for (int i = offset, n = out.size; i < n; i++) {
				final ShapePart p = out.get(i);
				if (Arrays.equals(p.transform.val, transform.val)) {
					part = p;
					break;
				}
			}
			if (part == null) {
				part = pool.obtain();
				part.parts.clear();
				part.transform.set(transform);
				out.add(part);
			}
			for (int i = 0, n = node.parts.size; i < n; i++)
				part.parts.add(node.parts.get(i).meshPart);
		}
		if (node.hasChildren()) {
			final boolean transformed = applyTransform && !Arrays.equals(transform.val, idt.val);
			final int o = transformed ? out.size : offset;
			getShapeParts(node.getChildren(), out, o, pool);
			if (transformed) {
				for (int i = o, n = out.size; i < n; i++) {
					final ShapePart part = out.get(i);
					tmpM.set(part.transform);
					part.transform.set(transform).mul(tmpM);
				}
			}
		}
	}

	public static <T extends Node> void getShapeParts (final Iterable<T> nodes, final Array<ShapePart> out, final int offset,
		final Pool<ShapePart> pool) {
		for (T node : nodes)
			getShapeParts(node, true, out, offset, pool);
	}

}
