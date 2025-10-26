(ns example.bytebuddy-adder
  (:import
    [net.bytebuddy ByteBuddy]
    [net.bytebuddy.dynamic.loading ClassLoadingStrategy$Default]
    [net.bytebuddy.implementation Implementation$Simple]
    [net.bytebuddy.implementation.bytecode ByteCodeAppender ByteCodeAppender$Size]
    [net.bytebuddy.jar.asm Opcodes Label Type]))

(defn make-adder-class []
  (let [appender
        (reify ByteCodeAppender
          (apply [_ visitor ctx _]
            (let [mv (.getMethodVisitor visitor)
                  l0 (Label.)]
              (.visitCode mv)
              (.visitLabel mv l0)
              (.visitVarInsn mv Opcodes/ILOAD 0)
              (.visitVarInsn mv Opcodes/ILOAD 1)
              (.visitInsn mv Opcodes/IADD)
              (.visitInsn mv Opcodes/IRETURN)
              (.visitMaxs mv 2 2)
              (.visitEnd mv)
              (ByteCodeAppender$Size. 2 2))))]
    (.. (ByteBuddy.)
        (subclass Object)
        (name "Adder")
        (defineMethod "add" Integer/TYPE (bit-or Opcodes/ACC_PUBLIC Opcodes/ACC_STATIC))
        (withParameters (into-array Class [Integer/TYPE Integer/TYPE]))
        ;; Wrap the appender in Implementation$Simple — that’s the missing piece
        (intercept (Implementation$Simple. appender))
        (make
          (.getClassLoader (clojure.lang.RT/baseLoader))
          ClassLoadingStrategy$Default/INJECTION))))

(def adder-class (make-adder-class))
(def add-method (.getMethod adder-class "add" (into-array Class [Integer/TYPE Integer/TYPE])))

(println "Result:" (.invoke add-method nil (into-array Object [7 5])))
