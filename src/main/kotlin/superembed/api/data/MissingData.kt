package uk.amaiice.superembed.api.data

object MissingData: IAPIData {
    override val statusCode: Int
       = 404
    override val message: String
        = "Unsupported URL"

}