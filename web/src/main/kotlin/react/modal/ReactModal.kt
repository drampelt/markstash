
package react.modal

import react.RClass
import react.RProps

@JsModule("react-modal")
@JsNonModule
external val ReactModal : ReactModalClass

external interface ReactModalProps : RProps {
    var isOpen: Boolean
    var onAfterOpen: () -> Unit
    var onAfterClose: () -> Unit
    var onRequestClose: () -> Unit
    var closeTimeoutMS: Long
    var contentLabel: String
    var portalClassName: String
    var overlayClassName: ModalTransitionClasses
    var id: String
    var className: ModalTransitionClasses
    var bodyOpenClassName: String
    var htmlOpenClassName: String
    var ariaHideApp: Boolean
    var shouldFocusAfterRender: Boolean
    var shouldCloseOnOverlayClick: Boolean
    var shouldCloseOnEsc: Boolean
    var role: String
}

external interface ModalTransitionClasses {
    var base: String
    var afterOpen: String
    var beforeClose: String
}

external interface ReactModalClass : RClass<ReactModalProps> {
    fun setAppElement(selector: String)
}
