package com.example.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import javax.microedition.khronos.opengles.GL11;

//Weak reference to a texture that stores the associated texture ID.
public class TextureReference extends WeakReference<Texture> {
	@SuppressWarnings("unchecked")
	public TextureReference(Texture texture, GL11 gl,
			ReferenceQueue referenceQueue, int textureId) {
		super(texture, referenceQueue);
		this.textureId = textureId;
		this.gl = gl;
	}

	public final int textureId;
	public final GL11 gl;
	public final DirectLinkedList.Entry<TextureReference> activeListEntry = new DirectLinkedList.Entry<TextureReference>(
			this);
}
